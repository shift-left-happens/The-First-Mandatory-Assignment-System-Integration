# SOAP Learning Guide

This guide explains SOAP through **our own code**, rather than in the abstract. Read it with the code
open next to you. Every concept points to a file you can find in `LibrarySoap/`.

Suggested order: sections 1–3 for the concepts, section 4 to follow one request through the
code, then the exercises in section 7.

---

## 1. SOAP in five minutes

SOAP is a **protocol for sending XML messages** between systems. A SOAP API has three parts:

| Piece | What it is | In our project |
|---|---|---|
| **Envelope** | The XML wrapper around every request and response | What Postman sends and receives |
| **WSDL** | A machine-readable description of the service: operations, messages, address | `http://localhost:5080/LibraryService.svc?singleWsdl` |
| **XSD** | XML Schema: the data types used inside messages (Book, Author, ...) | Embedded in the WSDL, generated from our `[DataContract]` classes |

### Compared with REST

| | REST | SOAP |
|---|---|---|
| URLs | One per resource (`/books/1`) | **One URL** for everything (`/LibraryService.svc`) |
| What to do | HTTP method (`GET`, `DELETE`, ...) | **Operation name** in the body + `SOAPAction` header |
| HTTP method | Varies | Always `POST` |
| Format | Usually JSON | Always XML |
| Contract | Optional (OpenAPI) | **Mandatory** (WSDL) |
| Errors | HTTP status codes (404, 409, ...) | `<Fault>` element, HTTP 500 |

### A real request

```http
POST /LibraryService.svc HTTP/1.1
Content-Type: text/xml; charset=utf-8
SOAPAction: "http://library.example/soap/LibraryService/GetBookById"
```
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Body>
    <GetBookById xmlns="http://library.example/soap">
      <id>1000</id>
    </GetBookById>
  </soapenv:Body>
</soapenv:Envelope>
```

### The response

```xml
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
  <s:Body>
    <GetBookByIdResponse xmlns="http://library.example/soap">
      <GetBookByIdResult>
        <Id>1000</Id>
        <Title>Harry Potter and the Sorcerer's Stone (Book 1)</Title>
        <AuthorId>394</AuthorId>
        <PublishingCompanyId>74</PublishingCompanyId>
        <PublishingYear>1976</PublishingYear>
      </GetBookByIdResult>
    </GetBookByIdResponse>
  </s:Body>
</s:Envelope>
```

What to notice:
- **`Envelope` / `Body`**: always there. (There is an optional `Header` for things like security tokens; we don't use it.)
- **`<GetBookById>`**: the element name *is* the operation. This style is called **document/literal wrapped**: the body holds one element named after the operation, and the parameters are its children.
- **`xmlns="http://library.example/soap"`**: our namespace. If it's missing or misspelled, the server won't recognise the operation. (It's defined once, at the top of `ILibraryService.cs`.)
- **The response** uses the naming convention `<Operation>Response` → `<Operation>Result`.
- **`SOAPAction`**: SOAP 1.1 uses this header to say which operation is called. It's `namespace/ServiceName/Operation`.

> **SOAP 1.1 vs 1.2:** we use 1.1 (`BasicHttpBinding` in `Program.cs`). 1.2 puts the action inside
> `Content-Type` instead of a separate header and uses a different envelope namespace. 1.1 is the most
> widely supported, which is why we chose it.

---

## 2. From C# attributes to the WSDL

We write C# and **CoreWCF generates the WSDL** from it. This is called **code-first**. (The opposite,
*contract-first*, means writing the WSDL/XSD by hand and generating code from it.)

| C# (our code) | Becomes in WSDL/XSD | Where |
|---|---|---|
| `[ServiceContract]` on `ILibraryService` | `<wsdl:portType name="LibraryService">` | `ILibraryService.cs` |
| `[OperationContract] Book GetBookById(int id)` | `<wsdl:operation name="GetBookById">` + request/response elements | `ILibraryService.cs` |
| `[DataContract] class Book` | `<xs:complexType name="Book">` | `Models.cs` |
| `[DataMember(Order = 1)] int Id` | `<xs:element name="Id" type="xs:int">` (in that order) | same |
| `[FaultContract(typeof(NotFoundFault))]` | `<wsdl:fault name="NotFoundFaultFault">` on that operation | `ILibraryService.cs` |
| `BasicHttpBinding` | `<wsdl:binding>` with `soap:binding` (SOAP 1.1 over HTTP) | `Program.cs` |
| `"/LibraryService.svc"` | `<soap:address location="http://localhost:5080/LibraryService.svc"/>` | `Program.cs` |

### Side by side

```csharp
// Models.cs
[DataContract(Namespace = Soap.Namespace)]
public class Book
{
    [DataMember(Order = 1)] public int Id { get; set; }
    [DataMember(Order = 2)] public string Title { get; set; } = "";
    ...
}
```
```xml
<!-- generated, from ?singleWsdl -->
<xs:complexType name="Book">
  <xs:sequence>
    <xs:element minOccurs="0" name="Id" type="xs:int"/>
    <xs:element minOccurs="0" name="Title" nillable="true" type="xs:string"/>
    ...
  </xs:sequence>
</xs:complexType>
```

`Order` matters. `xs:sequence` means the elements **must appear in that order** in the XML.

> **Code-first limitation:** see `minOccurs="0"`? It means every field is *optional* in the
> contract. And our rule "publishingYear ≥ 1900" doesn't appear in the XSD at all. Code-first
> can't express rules like `xs:minInclusive`, so we enforce them at runtime in C# and return a
> `ValidationFault`. A contract-first WSDL could state them in the contract itself. This is a
> good trade-off to discuss in the presentation.

### One contract, one file

All 14 operations live in `ILibraryService.cs`, grouped Books → Authors → Publishing companies.
That file *is* the WSDL in C# form: read it top to bottom next to `?singleWsdl` and every
`[OperationContract]` has a matching `wsdl:operation`. For a service this size, one contract is the
normal choice. A bigger system would split into several services (`IBookService`, `IAuthorService`, ...),
each with its own endpoint and WSDL.

---

## 3. Reading our WSDL

Open `http://localhost:5080/LibraryService.svc?singleWsdl` in a browser. It has five sections. Read them **bottom-up**:

| Section | Question it answers | Example from ours |
|---|---|---|
| `wsdl:service` | *Where* is it? | `<soap:address location="http://localhost:5080/LibraryService.svc"/>` |
| `wsdl:binding` | *How* do I talk to it? (protocol, style, SOAPAction) | `soapAction=".../GetBookById" style="document"`, `use="literal"` |
| `wsdl:portType` | *What* operations exist? | `GetBookById` with input, output and two faults |
| `wsdl:message` | What does each message contain? | `GetBookById_InputMessage` → element `tns:GetBookById` |
| `wsdl:types` | What do the data types look like? (the XSD) | `Book`, `Author`, `NotFoundFault`, ... |

`?wsdl` and `?singleWsdl` contain the same information. `?wsdl` splits the XSD into separate imported
files, while `?singleWsdl` inlines everything into one document, which is easier to read.

**Why have a WSDL at all?** Tools can generate a complete, typed client from it. In .NET:
`dotnet-svcutil http://localhost:5080/LibraryService.svc?wsdl`. Java, Python (zeep), SoapUI and
Postman ("Import → WSDL") can do the same. This strict, machine-readable contract is the
main selling point of SOAP.

---

## 4. One request, end to end

Let's trace `CreateBook` with a publishing year of 1850.

```
Postman
  │  POST /LibraryService.svc   SOAPAction: ".../CreateBook"   <CreateBook>...</CreateBook>
  ▼
CoreWCF  (Program.cs: UseServiceModel / BasicHttpBinding)
  │  1. Parses the envelope
  │  2. Uses SOAPAction to pick the operation → ILibraryService.CreateBook
  │  3. Deserialises <title>, <authorId>, ... into C# parameters
  │     (if <authorId>abc</authorId> → fails HERE, see section 5)
  │  4. Creates a LibraryService (via dependency injection)
  ▼
LibraryService.CreateBook            LibraryService.cs
  │  ValidateBook(...)
  │    ValidateText(title)
  │    ValidateId(authorId)
  │    publishingYear < 1900 ?  ──yes──►  throw Fault.Validation("publishingYear", ...)
  │    authors.Exists(authorId)      Repositories.cs  (SQL)
  │    publishers.Exists(...)
  │  books.Create(...)               Repositories.cs → INSERT ... last_insert_rowid()
  ▼
CoreWCF
  │  return value  → <CreateBookResponse><CreateBookResult>2001</CreateBookResult></CreateBookResponse>
  │  FaultException<ValidationFault>  → <s:Fault> with <detail><ValidationFault>...</ValidationFault></detail>
  ▼
Postman
```

Here is what each layer is responsible for:
- **CoreWCF**: everything SOAP-specific (XML, envelopes, WSDL). Our code never touches XML.
- **`LibraryService.cs`**: business rules (validation, existence checks, delete conflicts).
- **`Db.cs`**: opens connections to `library.dev.db`, a local copy of the shared database
  made on first start, so testing never changes the tracked `database/library.db`.
- **`Repositories.cs`**: SQL only. The old database has odd column names (`nBookID`, `cTitle`),
  and the repositories alias them (`nBookID AS Id`) so the rest of the code never sees them.

> **Why do we check references in C#?** The SQLite schema has **no foreign keys**. Nothing in the
> database stops a book from pointing at a non-existent author, so the service must enforce it.

---

## 5. Faults

A SOAP fault is SOAP's version of an error response:

```xml
<s:Fault>
  <faultcode>s:Client</faultcode>                       <!-- who is to blame: Client or Server -->
  <faultstring>'publishingYear' must be 1900 or later.</faultstring>   <!-- human-readable -->
  <detail>                                               <!-- typed, machine-readable -->
    <ValidationFault xmlns="http://library.example/soap">
      <Message>'publishingYear' must be 1900 or later.</Message>
      <Field>publishingYear</Field>
    </ValidationFault>
  </detail>
</s:Fault>
```

| Fault | When | Thrown in |
|---|---|---|
| `NotFoundFault` | The id doesn't exist | `Get*`, `Update*`, `Delete*` |
| `ValidationFault` | Missing or blank text, id ≤ 0, year < 1900, author/publisher doesn't exist | `Create*`, `Update*`, and every operation that takes an id |
| `ConflictFault` | Deleting an author or publisher that books still reference | `DeleteAuthor`, `DeletePublishingCompany` |

How they're made:
1. The fault class is a `[DataContract]` (`Faults.cs`), so it gets an XSD type like Book does.
2. `[FaultContract(typeof(...))]` on an operation **declares** in the WSDL that it can return that fault.
   Clients generated from the WSDL get a typed exception for it.
3. `throw Fault.NotFound("Book", id)` creates a `FaultException<NotFoundFault>`, and CoreWCF turns
   it into the XML above.

### Why is every fault HTTP 500?

The SOAP 1.1 spec says so. SOAP treats HTTP purely as a transport, so the real status lives in the
envelope. A REST client checks the status code, while a SOAP client parses the `<Fault>`.

### Two kinds of faults

There are **our** faults and **framework** faults:
- **Our faults** are `<detail><ValidationFault>`, `<detail><NotFoundFault>`, etc. We threw them on purpose.
- **Framework faults** come from CoreWCF *before our code runs*. For example, `<id>abc</id>` gives
  `faultcode = a:DeserializationFailed`, because "abc" isn't an `xs:int`. A wrong namespace or an
  unknown SOAPAction gives a similar fault. The Postman request
  *"Framework fault - id is not an integer"* demonstrates this.

Unexpected exceptions (like a database crash) become a generic `InternalServiceFault`
that hides the details. That's intentional, so stack traces don't leak to clients. During debugging you
can temporarily add `IncludeExceptionDetailInFaults = true` to `[ServiceBehavior]` in
`LibraryService.cs` to see the real error.

---

## 6. Pros and cons (for the presentation)

These are things we actually ran into while building this project, rather than generic points.

### Pros
- **The contract is the documentation.** The WSDL fully describes the API, so tools generate typed clients
  in any language with zero hand-written code.
- **Typed errors.** Faults are declared per operation in the contract, and clients know exactly which
  errors to expect and what fields they carry.
- **We never wrote any XML-handling code.** CoreWCF does serialisation, routing and the WSDL.
- **Strict input checking for free.** Sending `abc` as an int is rejected by the framework.

### Cons
- **Verbose.** `GetBookById(1000)` takes about 250 bytes of envelope. The same call in REST is `GET /books/1000`.
- **One URL, always POST, always HTTP 500 on errors.** HTTP caching, status codes and browser
  tooling don't help you. You can't test it by opening a URL in a browser.
- **Code-first contracts are loose.** Everything is `minOccurs="0"`, and rules like year ≥ 1900 can't be
  expressed in the XSD, so they live in C# only. Contract-first fixes this but is far more work.
- **Fragile by hand.** One wrong namespace or a missing `SOAPAction` and nothing works. Hand-written
  Postman requests are tedious compared with REST or GraphQL.
- **Poor fit for browsers and frontends.** Calling it from JavaScript means building XML strings.

### Where SOAP still makes sense
Enterprise and B2B integrations (banking, government, insurance, airlines), legacy systems, and cases where
WS-* standards (WS-Security, reliable messaging, transactions) or a strict signed contract are required.

---

## 7. Exercises

Each takes 5–15 minutes.

1. **Break the envelope.** In Postman, change the namespace in a request to `http://wrong`. What fault
   do you get? Then remove the `SOAPAction` header. Then send `<id>abc</id>`.
2. **Read the contract.** In `?singleWsdl`, find which faults `DeleteAuthor` can return. Compare
   with `[FaultContract]` in `ILibraryService.cs`.
3. **Make a field required.** Change `[DataMember(Order = 2)]` on `Book.Title` (in `Models.cs`) to
   `[DataMember(Order = 2, IsRequired = true)]`. Restart and look at the WSDL: what happened to `minOccurs`?
4. **Add an operation.** Add `Book[] ListBooks()` (`ILibraryService.cs` → `LibraryService.cs` →
   a `SELECT` in `BookRepository`). Check it appears in the WSDL and call it from Postman.
5. **Generate a client.** Run `dotnet tool install -g dotnet-svcutil` and then
   `dotnet-svcutil http://localhost:5080/LibraryService.svc?wsdl` in an empty console project.
   Look at the generated C#. This is the "contract → client" story.
6. **Compare with REST.** When your teammates' REST API is running, send the same "get book 1000" to
   both and compare request and response size and readability.

---

## Cheat sheet

| I want to... | Go to |
|---|---|
| Change the URL, port or binding | `Program.cs`, `Properties/launchSettings.json` |
| Add/change an operation | `ILibraryService.cs` + `LibraryService.cs` |
| Change a data type | `Models.cs` |
| Change SQL | `Repositories.cs` |
| Change validation rules or faults | `LibraryService.cs` (bottom), `Faults.cs` |
| Change the database path | `appsettings.json` (`Database:Source`, `Database:WorkingCopy`) |
| Reset the data | Stop the service, delete `LibrarySoap/library.dev.db` |
