# Bibliotekets gRPC-service

Dette er opgavens gRPC-del. En Node.js-server tilbyder tre metoder til bøger og bruger SQLite-filen `library.db`. Et ekstra program, `replica.js`, viser hvordan en anden proces kan lytte efter nye bøger og gemme dem i sin egen database. Det ekstra program er en **gRPC-klient**, ikke endnu en gRPC-server.

## Kort fortalt: Hvordan virker gRPC her?

Serverens kontrakt er defineret i [`library.proto`](library.proto). Filen beskriver metoder, forespørgsler og svar. gRPC sender beskederne som Protocol Buffers over HTTP/2. I Postman skriver man beskeden som JSON; Postman bruger enten en importeret `.proto`-fil eller server reflection til at kode den til Protocol Buffers, før den sendes.

```text
Postman ── CreateBook ──► server.js ── gemmer ──► library.db
                           │
                           └── ny bog ──► WatchBooks-klienter
                                            │
                                            └── replica.js ──► replica.db
```

`server.js` indlæser `.proto`-filen med `@grpc/proto-loader` og knytter de tre metoder til JavaScript-funktioner. Det kaldes dynamisk indlæsning: projektet har ingen separat genereret stub-fil. `replica.js` spørger i stedet serverens reflection-service efter `library.LibraryService`, bygger en klient ud fra svaret og kalder `WatchBooks`. Kopien indlæser altså ikke den lokale `library.proto`.

Serveren tilbyder også **server reflection**. Det er en ekstra gRPC-service, som lader et værktøj spørge den kørende server om dens service, metoder og beskedtyper. Reflection er valgfrit i gRPC og er her slået til for at gøre demonstrationen lettere.

| Metode | Type | Hvad sker der? |
|---|---|---|
| `GetBookById` | Unary: ét kald, ét svar | Serveren slår en bog op i `library.db` og returnerer den. |
| `CreateBook` | Unary: ét kald, ét svar | Serveren validerer data, gemmer bogen og returnerer dens nye ID. |
| `WatchBooks` | Server-streaming | Klienten holder forbindelsen åben og modtager bøger, som senere oprettes gennem serveren. |

Når en klient kalder `WatchBooks`, lægger serveren forbindelsen i et `Set` af aktive abonnenter. Efter et vellykket `CreateBook` sender serveren den nye bog til alle forbindelser i sættet. Når en klient afbryder, fjernes forbindelsen igen. Man behøver **ikke** abonnere for at kalde `GetBookById` eller `CreateBook`.

## Filer og databaser

- [`library.proto`](library.proto): serverens kontrakt; kan også importeres manuelt i Postman.
- [`server.js`](server.js): gRPC-server, validering, SQLite-kald og abonnentliste.
- [`library.db`](library.db): serverens eksisterende SQLite-database med bøger, forfattere og forlag.
- [`replica.js`](replica.js): separat klient, der lytter på `WatchBooks` og gemmer modtagne bøger.
- `replica.db`: oprettes automatisk af `replica.js`; indeholder kun bøger, som klienten har modtaget.
- [`replica-list.js`](replica-list.js): viser de seneste 20 bøger i `replica.db`.
- [`server.test.js`](server.test.js): integrationstest med midlertidige databaser.

Serveren og kopien bruger **to forskellige SQLite-filer**. De øvrige API’er i opgaven behøver heller ikke bruge disse filer.

## Start serveren

Kræver Node.js 20 eller nyere, npm og `library.db` i `grpc/`.

```powershell
cd grpc
npm install
npm start
```

Serveren lytter på `127.0.0.1:50051`. Kommandoerne herunder skal også køres fra `grpc/`. Brug `GRPC_PORT` til en anden port og `LIBRARY_DB` til en anden **eksisterende** SQLite-fil, hvis det er nødvendigt.

## Prøv metoderne i Postman

1. Opret en **gRPC-request** med adressen `localhost:50051`.
2. Under **Service definition** importerer du [`library.proto`](library.proto).
3. Vælg en metode under `library.LibraryService`.
4. Skriv beskeden i **Message**, og klik **Invoke**.

Brug feltnavnene **præcis som i `.proto`-filen**, altså med understregninger. `server.js` ser dem som camelCase, fordi Node.js-biblioteket omdanner navnene efter modtagelsen. Derfor skrives `author_id` i Postman, men `authorId` inde i serverkoden.

**GetBookById:**

```json
{ "id": 1000 }
```

**CreateBook:**

```json
{
  "title": "Min demo-bog",
  "author_id": 1,
  "publishing_company_id": 1,
  "publishing_year": 2020
}
```

**WatchBooks:**

```json
{}
```

Lad `WatchBooks` være åben i én Postman-fane, og send `CreateBook` i en anden. Den nye bog vises i `WatchBooks`-fanen. Åbn gerne to `WatchBooks`-faner for at vise, at begge abonnenter får bogen. ID `1` findes for både forfatter og forlag i den medfølgende database.

[`postman/grpc.postman_environment.json`](postman/grpc.postman_environment.json) kan importeres som Postman-miljø. [`postman/grpc-examples.json`](postman/grpc-examples.json) er en liste over beskeder, som kan kopieres ind i Postman; den er **ikke** en importérbar Postman-collection.

### Vis server reflection i Postman

1. Genstart serveren med `Ctrl+C` og `npm start`, hvis den kørte før reflection blev tilføjet.
2. Opret en **helt ny gRPC-request** i Postman og angiv `localhost:50051`.
3. Undlad at importere `library.proto` i denne nye request. Klik på **Select a method**.
4. Postman kan nu hente servicebeskrivelsen fra serveren og vise `library.LibraryService` med `GetBookById`, `CreateBook` og `WatchBooks`.

Forskellen kan demonstreres ved at sammenligne denne request med den gamle, hvor `.proto`-filen blev importeret manuelt. Reflection ændrer ikke bogmetoderne; det gør kontrakten synlig for værktøjer, der understøtter reflection.

Ved oprettelse kræves en titel på 1–255 tegn, eksisterende forfatter og forlag samt udgivelsesår fra 1900. Ugyldige værdier giver `INVALID_ARGUMENT`. Et opslag på en bog, der ikke findes, giver `NOT_FOUND`.

## Demo: En klient gemmer i sin egen database

Brug tre terminaler, alle placeret i `grpc/`:

1. **Terminal 1:** `npm start` starter bogserveren.
2. **Terminal 2:** `npm run replica` starter lytte-klienten. Vent på `Discovered LibraryService through reflection. Listening for new books`.
3. **Postman:** Send `CreateBook` med en ny titel. Terminal 2 skriver `Saved book ...`.
4. **Terminal 3:** `npm run replica:list` viser den nye bog i `replica.db`.

Scriptnavnet er `replica`, så kommandoen er **`npm run replica`**, ikke `npm run replica.js`. Hvis serveren bruger en anden adresse, kan klienten starte med fx `$env:GRPC_ADDRESS='127.0.0.1:50052'; npm run replica`. `REPLICA_DB` kan ændre kopiens databasefil.

Kopien skal kunne nå serverens reflection-service ved start. Den modtager kun bøger oprettet **efter** den begyndte at lytte. Den indhenter ikke gamle eller mistede bøger. Bøger indsat direkte i SQLite eller af en anden proces udløser heller ikke en `WatchBooks`-besked. Dette er en demonstration af live-beskeder, ikke en fuld løsning til synkronisering af databaser.

## Fordele og ulemper ved gRPC i dette projekt

| Fordele | Ulemper |
|---|---|
| `.proto` giver en tydelig, typet kontrakt, som klienter kan importere eller opdage via reflection. | En klient skal have kontrakten eller kunne hente den via reflection; det kræver ekstra opsætning sammenlignet med et simpelt HTTP-kald. |
| Protocol Buffers er et kompakt binært format. | Beskederne er mindre læsbare uden værktøj end REST/JSON. |
| Server-streaming gør det enkelt at sende nye bøger til åbne klienter. | En live stream gemmer ikke hændelser til klienter, som er offline. |
| En anden Node.js-proces kan bruge samme kontrakt som klient. | Opsætning og fejlsøgning er mere omfattende end et simpelt HTTP-endpoint. |

For bibliotekseksemplet er `WatchBooks` den tydeligste fordel: klienten får en ny bog uden gentagne opslag. Til gengæld kræver en pålidelig kopi af hele databasen ekstra mekanismer til første indlæsning og indhentning af mistede ændringer.

### Hvad ville en driftssat service typisk kræve mere?

Denne løsning opfylder opgavens tre gRPC-metoder, men den er en lokal demonstration. Den bruger en ukrypteret forbindelse på `127.0.0.1` og har ingen adgangskontrol. Hvis den skulle kunne kaldes over et netværk, skulle man tage stilling til TLS, autentifikation og om reflection skal være tilgængelig for alle klienter. Man ville også typisk tilføje deadlines/timeouts, health checks, logning og håndtering af overbelastning.

`WatchBooks` holder abonnenter i hukommelsen. Genstart af serveren eller afbrydelse af kopien kan derfor medføre mistede hændelser. En løsning, der lover fuld synkronisering, kræver en første indlæsning og en vedvarende hændelseslog eller anden mekanisme til at indhente mistede ændringer. Dette er en begrænsning ved den konkrete implementering, ikke et krav om at alle gRPC-services skal bygges sådan.

## Test

```powershell
npm test
```

Testen opretter midlertidige SQLite-filer og kontrollerer opslag, validering, oprettelse, to samtidige abonnenter, lagring i kopi-databasen og at reflection annoncerer servicen. Den ændrer ikke `library.db` eller en eksisterende `replica.db`.
