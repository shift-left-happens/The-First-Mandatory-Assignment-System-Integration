# GraphQL Learning Guide

This guide explains GraphQL through **our own code**, rather than in the abstract. Read it with the
code open next to you. Every concept points to a file you can find in `graphql/`.

Suggested order: sections 1–3 for the concepts, section 4 to follow one request through the code,
then the exercises in section 8.

---

## 1. GraphQL in five minutes

GraphQL is a **query language and runtime for APIs**. Instead of many URLs (REST) or one URL with
XML operations (SOAP), it has **one URL** that accepts a **query describing exactly the data you
want**.

| Piece | What it is | In our project |
|---|---|---|
| **Endpoint** | The single HTTP URL | `POST http://localhost:4000/graphql` (`src/index.js`) |
| **Schema (SDL)** | The typed contract: types, queries, mutations | `src/schema.js` |
| **Resolvers** | Functions that produce each field's value | `src/resolvers.js` |
| **GraphiQL** | A browser IDE for exploring the schema | Served when you open `/graphql` in a browser |

### Compared with REST

| | REST | GraphQL |
|---|---|---|
| URLs | One per resource (`/books/1`, `/authors`) | **One URL** for everything (`/graphql`) |
| HTTP method | `GET`, `POST`, `DELETE`, ... | Almost always `POST` |
| What to do | Method + URL | Operation name in the query (`book`, `createBook`) |
| Response shape | Fixed by the server | **Chosen by the client** |
| Over/under-fetching | Common | Avoided (you ask for exactly what you need) |
| Errors | HTTP status codes (404, 409, ...) | `errors` array in the JSON body (usually HTTP 200) |
| Contract | Optional (OpenAPI) | **Mandatory** (the schema) |

### A real request

```http
POST /graphql HTTP/1.1
Content-Type: application/json
```
```json
{
  "query": "query { book(id: 1000) { id title author { name surname } } }"
}
```

### The response

```json
{
  "data": {
    "book": {
      "id": 1000,
      "title": "Harry Potter and the Sorcerer's Stone (Book 1)",
      "author": { "name": "J. K.", "surname": "Rowling" }
    }
  }
}
```

What to notice:
- **One URL, one method.** Everything is `POST /graphql`.
- **The query is shaped like the response.** You write the JSON you want (minus the values).
- **`book(id: 1000)`** is a *field* on the root `Query` type; **`author { ... }`** is a *nested field*.
- The response echoes the query structure under `data`, keyed by the same field names.
- There is no `/books/1000` URL. The operation name and its arguments live in the body.

## 2. From SDL to the running server

We write the API in two files: the **schema** (what the API offers) and the **resolvers** (how each
field is produced). GraphQL Yoga wires them together and serves the endpoint.

| File | Responsibility |
|---|---|
| `src/schema.js` | The SDL: types (`Book`, `Author`, `Publisher`), `Query`, `Mutation`, the `SearchResult` union |
| `src/resolvers.js` | One function per field that needs logic (validation + calls into the repository) |
| `src/repository.js` | The SQL. Maps DB columns (`nBookID`, `cTitle`, ...) to GraphQL fields (`id`, `title`, ...) |
| `src/db.js` | Opens the shared SQLite database and creates the tables if missing |
| `src/errors.js` | Builds `GraphQLError`s with a machine-readable `extensions.code` |
| `src/index.js` | Creates the Yoga server and listens on the port |

### SDL and resolvers, side by side

```graphql
# schema.js
type Query {
  book(id: Int!): Book
}
```
```js
// resolvers.js
Query: {
  book: (_parent, { id }) => {
    const book = repository.findBookById(id);
    if (!book) throw notFound(`Book with id ${id} was not found.`);
    return book;
  },
},
```

Every **field** can have a resolver, but only some need one:
- **Root fields** (`Query.*`, `Mutation.*`) always have a resolver.
- **Scalar fields** like `Book.id` and `Author.name` are read straight from the object the parent
  resolver returned — no resolver needed.
- **Relationship fields** (`Author.books`, `Publisher.authors`, ...) have their own resolvers, which
  is exactly what makes nesting work (section 4).

### How one request is handled

```
Postman / GraphiQL
  |  POST /graphql   { query, variables }
  v
GraphQL Yoga              (src/index.js)
  |  1. Parse the query
  |  2. Validate it against the schema  -> unknown field? error HERE (HTTP 200)
  |  3. Coerce variables                -> missing/wrong variable? error HERE (HTTP 400)
  |  4. Execute: call the root resolver (e.g. Query.book)
  v
resolvers.js  ->  repository.js  ->  SQLite (src/db.js)
  |  Resolve nested fields on demand (author, publisher, books, ...)
  v
GraphQL Yoga
  |  Build the { data, errors } JSON in the shape the client asked for
  v
Postman / GraphiQL
```

> **Steps 2 and 3 matter.** A bad *query* (typo in a field name) is a validation error and comes
> back with **HTTP 200** plus an `errors` array. A bad *request* (a missing required variable, or a
> variable of the wrong type) is rejected earlier with **HTTP 400**. The Postman collection's
> **Disadvantages** folder demonstrates both.

## 3. Reading our schema

Open `http://localhost:4000/graphql` in a browser and click the **Docs** tab (or send the **Schema
Introspection** request from the Postman collection). You get the same picture that the SDL in
`src/schema.js` describes:

| Section | Question it answers | Example from ours |
|---|---|---|
| `type Query` | What can I **read**? | `book(id)`, `authors`, `publishers`, `search(term)` |
| `type Mutation` | What can I **write**? | `createBook`, `updateBook`, `deleteBook`, ... |
| `type Book` / `Author` / `Publisher` | What do the objects look like? | `Book.title`, `Author.surname`, `Publisher.name` |
| `union SearchResult` | Which types can `search` return? | `Book \| Author \| Publisher` |
| `scalar Int` / `String` / `Boolean` | The leaf values | `id: Int!`, `title: String!` |

### Types and the `!` (non-null)

```graphql
type Book {
  id: Int!                 # always present, never null
  title: String!
  publishingYear: Int!
  author: Author           # may be null (nullable relationship)
  publisher: Publisher
}
```

- `Int!` means **non-null**: the field can never be `null`.
- `[Author!]!` (on `Publisher.authors`) means a **non-null list of non-null authors**: the list
  itself is never null (an authorless publisher returns `[]`) and it contains no `null` entries.
- `author: Author` (no `!`) is nullable — it *could* be null, so clients must handle that.

The schema also carries **docstrings** (the `"..."` lines), which show up as descriptions in
GraphiQL's Docs panel.

### Introspection

Clients do not need our source to use the API: they can ask the server to describe itself.

```graphql
query {
  __schema {
    types { name kind }
  }
}
```

This is the same information Postman and code generators use to produce typed clients — the GraphQL
equivalent of SOAP's WSDL. It is also why GraphQL needs **no versioning**: a client asks only for
fields it knows about, so we can add fields and types without breaking anyone.

## 4. Nesting: Publisher → Authors → Books

This is the feature the assignment asked us to add. One request can walk the whole graph:

```graphql
query {
  publishers {
    id
    name
    authors {
      id
      name
      surname
      books {
        id
        title
        publishingYear
      }
    }
  }
}
```

A trimmed response (publisher 74 in our data):

```json
{
  "data": {
    "publishers": [
      {
        "id": 74,
        "name": "Sanford LLC",
        "authors": [
          {
            "id": 64,
            "name": "Dan",
            "surname": "Brown",
            "books": [
              { "id": 1011, "title": "The Da Vinci Code", "publishingYear": 2003 },
              { "id": 1024, "title": "Angels & Demons", "publishingYear": 2000 }
            ]
          }
        ]
      }
    ]
  }
}
```

### How the nesting actually resolves

```
Query.publishers                 (resolvers.js) -> listPublishers()            -> SELECT * FROM tpublishingcompany
  Publisher.authors              (resolvers.js) -> listAuthorsByPublisherId() -> SELECT DISTINCT a.* JOIN tbook ...
    Author.books                 (resolvers.js) -> listBooksByAuthorId()       -> SELECT * FROM tbook WHERE nAuthorID = ?
      Book.author / Book.publisher (resolvers.js) -> findAuthorById() / findPublisherById()
```

The key idea: **a resolver runs only when the client selects that field.** The `Author.books`
resolver is not called at all unless the query asks for `books`. That is what makes GraphQL
efficient for the client — it is never sent data it did not ask for.

### The catch (worth a slide in the presentation)

Nesting is not free, and a nested list is **not scoped to its parent**:

- **N+1 queries.** `publishers { authors { books } }` runs one query for the publishers, then one per
  publisher, then one per author. For our 158 publishers that is hundreds of `SELECT`s. Big systems
  fix this with **batching** (e.g. Facebook's DataLoader), caching, and depth/complexity limits.
- **Unscoped lists.** In `publishers { authors { books } }`, `books` returns **all** of an author's
  books, not only the ones published by the enclosing publisher. J. K. Rowling (id 394) has Harry
  Potter books under publishers 74, 48 and 11, so her `books` list appears under each of those
  publishers. This is normal GraphQL behaviour (each field is resolved independently), but it
  surprises people — which is exactly why `notes.md` lists *"nesting can be problematic"*.
- **Large payloads.** A deeply nested query can return megabytes in one response, pushing cost from
  the server onto the network and the client.

## 5. Mutations, variables, and errors

### Mutations

Writes use the same shape as queries, under `type Mutation`:

```graphql
mutation CreateBook($title: String!, $authorId: Int!, $publishingCompanyId: Int!, $publishingYear: Int!) {
  createBook(title: $title, authorId: $authorId, publishingCompanyId: $publishingCompanyId, publishingYear: $publishingYear) {
    id
    title
  }
}
```
```json
{ "title": "New Book", "authorId": 1, "publishingCompanyId": 1, "publishingYear": 2024 }
```

Our mutations return the created/updated object (not just an id), so the client can read the new
state immediately without a second request.

### Variables vs. inline values

- **Variables** (`$title`, `$id`) are typed in the operation signature (`$id: Int!`) and supplied in
  the `variables` JSON. This keeps the query string reusable and avoids string-building bugs.
- **Inline values** (`book(id: 1000)`) are fine for fixed, simple cases.

### Errors: our codes

Every expected failure is thrown from `resolvers.js` as a `GraphQLError` with a machine-readable
`extensions.code` (see `src/errors.js`):

| Code | Meaning | Mirrors (SOAP) |
|---|---|---|
| `VALIDATION_ERROR` | Missing/blank text, year < 1900, author/publisher does not exist | `ValidationFault` |
| `NOT_FOUND` | The requested id does not exist | `NotFoundFault` |
| `CONFLICT` | Deleting an author/publisher that books still reference | `ConflictFault` |

Example (querying a book that does not exist):

```json
{
  "errors": [
    {
      "message": "Book with id 999999 was not found.",
      "path": ["book"],
      "extensions": { "code": "NOT_FOUND" }
    }
  ],
  "data": { "book": null }
}
```

What to notice:
- The HTTP status is **200**. The failure lives in the JSON, not in the status line.
- `data.book` is `null` because that field failed, but other fields in the same request could still
  succeed (partial success).
- Our own codes (`NOT_FOUND`, ...) sit next to framework ones (`GRAPHQL_VALIDATION_FAILED` for a bad
  field, or no code at all for a variable-coercion error).

> **Compare with SOAP:** SOAP puts the error in a `<Fault>` and always uses HTTP 500. GraphQL puts it
> in the `errors` array and (for execution errors) uses HTTP 200. Both keep the transport simple and
> the real error in the payload; GraphQL just does not reserve a status code for it.

## 6. Features worth showing (the Postman "Advantages" folder)

Each of these maps to a point in `notes.md`, and each has a ready-made request in the Postman
collection.

| Feature | Point in `notes.md` | Postman request |
|---|---|---|
| Granularity (ask for exactly the fields you need) | *"Granularity when fetching"* | Advantages → Granularity |
| Nesting (one request walks the graph) | *"easy nesting"*, *"less latency"* | Advantages → Nesting |
| Fragments (reusable field sets) | *"Fragments for reuseability"* | Advantages → Fragments |
| Aliases (many resources in one round trip) | *"less latency"* | Advantages → One round trip |
| Unions (heterogeneous results) | *"GraphQL Unions"* | Advantages → Unions |
| Introspection (no versioning) | *"No versioning"* | Advantages → No versioning |

### Granularity

```graphql
query { publishers { id } }              # tiny
query { publishers { id name } }         # a bit more
```

Same field, different selections. The client pays only for what it asks.

### Fragments

```graphql
query {
  publishers {
    ...PublisherSummary
    authors { ...AuthorSummary books { ...BookSummary } }
  }
}
fragment PublisherSummary on Publisher { id name }
fragment AuthorSummary on Author { id name surname }
fragment BookSummary on Book { id title publishingYear }
```

A fragment is a named set of fields on a type. Define it once and reuse it in many queries — handy
when several parts of a UI need the same shape.

### Aliases

```graphql
query Dashboard {
  featuredBook: book(id: 1000) { id title }
  allAuthors: authors { id name }
  allPublishers: publishers { id name }
}
```

Aliases rename fields and let one request fetch several independent resources — fewer round trips.

### Unions

```graphql
query {
  search(term: "Rowling") {
    __typename
    ... on Book { id title }
    ... on Author { id name surname }
    ... on Publisher { id name }
  }
}
```

`search` returns a `SearchResult` union, so one list can hold books, authors and publishers. The
`__resolveType` function in `resolvers.js` decides which concrete type each hit is; the client uses
`__typename` plus inline fragments to read the right fields.

### Introspection / no versioning

See section 3. The schema is the contract; there is no `/v2`.

## 7. Pros and cons (for the presentation)

These are things we actually hit while building this API, not generic points.

### Pros
- **The client gets exactly what it needs.** No over-fetching (unlike fixed REST resources) and no
  under-fetching (one nested request instead of several).
- **One endpoint, one round trip.** `Publisher → authors → books` is a single `POST /graphql`, so
  fewer round trips and lower latency on high-latency links.
- **Typed, self-documenting contract.** The SDL plus introspection give GraphiQL docs and typed
  clients for free — and allow adding fields without breaking clients (**no versioning**).
- **Reusable pieces.** Fragments, variables and aliases keep queries DRY and testable.
- **Unions model "one of several types" cleanly** (our `SearchResult`), which is awkward in REST.

### Cons
- **Caching is harder.** Everything is `POST` to one URL, so HTTP/CDN caching by URL + method does
  not work. Caching moves to the client (e.g. Apollo) or a custom layer.
- **Nesting can be problematic.** N+1 queries, unscoped nested lists, and huge payloads (section 4).
  You need depth/complexity limits and batching to keep it healthy.
- **Learning curve / complexity.** Schema + resolvers + validation is more moving parts than a plain
  REST handler; concepts like unions, interfaces and fragments take time to learn.
- **Not RESTful.** No resource URLs, no `GET`/`DELETE` semantics, and no status codes for errors
  (`200` with an `errors` array). Browser and HTTP tooling help less.
- **Dynamic queries complicate the server.** Clients can ask for expensive shapes, so the server must
  protect itself (limits, timeouts, query-cost analysis).
- **Tooling outside JavaScript is thinner.** The reference implementation is JS; other languages have
  good but fewer, less mature libraries, especially for advanced tooling.

### Where GraphQL shines for this scenario

A library front-end that shows a publisher page with its authors and their books: one nested query
replaces several REST calls and returns exactly the fields the screen needs. The trade-off is server
complexity (batching, limits) that REST does not need.

---

## 8. Exercises

Each takes 5–15 minutes.

1. **Over-fetch vs. under-fetch.** Run `query { publishers { id name } }` and the nested
   `publishers { authors { books } }`. Compare the response sizes. Which fields did you not ask for
   in the first query?
2. **Break a query.** Send `query { books { id } }`. What error do you get, and is the HTTP status
   200 or 400? Then send a query missing a required variable and compare the status.
3. **Watch the SQL.** Add a `console.log` in `listBooksByAuthorId` (in `src/repository.js`) and run
   the nested publisher query. How many times does it print? That is the N+1 problem.
4. **Add a field.** Add a `bookCount` field to `type Author` in `src/schema.js` and a resolver that
   returns `repository.listBooksByAuthorId(parent.id).length`. Try it from GraphiQL.
5. **Filter a list.** Change `Query.authors` to `authors(name: String): [Author!]!` and filter in the
   resolver/repository. Notice that old queries without the argument still work (backwards
   compatibility = no versioning).
6. **Reuse a fragment.** Write a `fragment BookCore on Book { id title publishingYear }` and use it
   in two different queries.
7. **Compare with SOAP/REST.** Run the same "get book 1000 with its author" against the SOAP and REST
   APIs and compare request/response size and readability.

---

## Cheat sheet

| I want to... | Go to |
|---|---|
| Change the port, host or endpoint path | `src/index.js` |
| Add/change a type, query or mutation | `src/schema.js` |
| Add/change business rules, validation or errors | `src/resolvers.js` |
| Change SQL or add a relationship query | `src/repository.js` |
| Change the database path or tables | `src/db.js` (`DB_PATH`, `ensureSchema()`) |
| Change an error code | `src/errors.js` |
| Run the API | `npm start` (or `docker compose up --build`) |
| Explore the schema | open `http://localhost:4000/graphql` (GraphiQL Docs) |
| Run the automated tests | `npm run smoke` (expect `22 passed, 0 failed`) |
| Demo advantages/disadvantages | Postman: **Advantages (notes.md)** / **Disadvantages (notes.md)** |






