# GraphQL API

The GraphQL API exposes the library system through a single HTTP endpoint:

```text
POST http://localhost:4000/graphql
```

It supports the three data types required by the assignment (`Book`, `Author`,
`Publisher`), three queries and nine mutations (create / update / delete for
each type).

---

# Tech stack

| Component | Choice | Why |
|---|---|---|
| Runtime | **Node.js 24** | Common, easy to read, ships with a built-in SQLite driver |
| GraphQL server | **GraphQL Yoga** | Minimal boilerplate, schema defined in plain SDL, GraphiQL IDE built in |
| Database driver | **`node:sqlite`** | Built into Node, so **no native dependencies** and a simple Docker image |
| Container | **Docker + Docker Compose** | One command to run, identical on every machine |

---

# Running with Docker (recommended)

From the `graphql/` directory:

```bash
docker compose up --build
```

The API is then available at:

```text
http://localhost:4000/graphql
```

The `docker-compose.yml` mounts the repository's shared `../database` folder into
the container, so this API reads and writes the **same** `library.db` as the
other APIs.

To stop it:

```bash
docker compose down
```

---

# Running locally (without Docker)

Requires **Node.js >= 22.5** (for the built-in `node:sqlite` module).

```bash
npm install
npm start
```

The server starts on port `4000` and uses `../database/library.db` by default.

Configuration can be overridden with environment variables:

| Variable | Default | Description |
|---|---|---|
| `PORT` | `4000` | Port the HTTP server listens on |
| `HOST` | `0.0.0.0` | Interface to bind |
| `DB_PATH` | `../database/library.db` | Path to the SQLite database file |

---

# Schema

```graphql
type Book {
  id: Int!
  title: String!
  publishingYear: Int!
  author: Author
  publisher: Publisher
}

type Author {
  id: Int!
  name: String!
  surname: String!
  books: [Book!]!
}

type Publisher {
  id: Int!
  name: String!
  authors: [Author!]!
  books: [Book!]!
}

union SearchResult = Book | Author | Publisher

type Query {
  book(id: Int!): Book
  authors: [Author!]!
  publishers: [Publisher!]!
  search(term: String!): [SearchResult!]!
}

type Mutation {
  createBook(title: String!, authorId: Int!, publishingCompanyId: Int!, publishingYear: Int!): Book!
  updateBook(id: Int!, title: String!, authorId: Int!, publishingCompanyId: Int!, publishingYear: Int!): Book!
  deleteBook(id: Int!): Boolean!

  createAuthor(name: String!, surname: String!): Author!
  updateAuthor(id: Int!, name: String!, surname: String!): Author!
  deleteAuthor(id: Int!): Boolean!

  createPublisher(name: String!): Publisher!
  updatePublisher(id: Int!, name: String!): Publisher!
  deletePublisher(id: Int!): Boolean!
}
```

> `Book` no longer exposes the raw foreign keys (`authorId` /
> `publishingCompanyId`). A book's author and publisher are read through the
> `author` / `publisher` fields. The mutations still take `authorId` and
> `publishingCompanyId` as **arguments**, because a create/update has to say which
> author and publisher to point at.

---

# Relationships and nesting

On top of the assignment's operations, the schema exposes the relationships
between the three types so that a single request can walk the graph:

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

What each relation returns:

| Field | Returns |
|---|---|
| `Book.author` / `Book.publisher` | The author / publisher referenced by the book's foreign keys |
| `Author.books` | Every book written by the author |
| `Publisher.authors` | The **distinct** authors of the books this publisher publishes |
| `Publisher.books` | Every book published by the publisher |

> **Nesting caveat.** A nested list is **not** scoped to its parent. In
> `publishers { authors { books } }` the `books` field returns *all* books by
> that author, not only the ones from the enclosing publisher. This is normal
> GraphQL behaviour and a good point to discuss in the presentation (see the
> **Disadvantages** folder in the Postman collection).

`search(term)` returns a `SearchResult` **union** (`Book | Author | Publisher`),
which clients narrow with inline fragments:

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

The schema is defined in **`src/schema.js`**. At runtime it can also be retrieved
through GraphQL **introspection**:

- open `http://localhost:4000/graphql` and use the **Docs** panel in GraphiQL, or
- send the **Schema Introspection** request from the included Postman collection.

---

# Viewing the schema

The quickest way is the **Schema Introspection** request in the Postman
collection (folder `Schema`). It returns every type, field and argument, for
example:

```graphql
query Introspection {
  __schema {
    queryType { name }
    mutationType { name }
    types {
      kind
      name
      fields {
        name
        args { name }
        type { kind name ofType { kind name ofType { kind name } } }
      }
    }
  }
}
```

---

# Queries

## Get a book by ID

```graphql
query {
  book(id: 1000) {
    id
    title
    publishingYear
    author { id name surname }
    publisher { id name }
  }
}
```

## List all authors

```graphql
query {
  authors {
    id
    name
    surname
  }
}
```

## List all publishers

```graphql
query {
  publishers {
    id
    name
  }
}
```

## List publishers with their authors and books (nesting)

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

## Search across books, authors and publishers (union)

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

---

# Mutations

## Create a book

```graphql
mutation {
  createBook(
    title: "Example Book"
    authorId: 1
    publishingCompanyId: 1
    publishingYear: 2024
  ) {
    id
    title
  }
}
```

## Update a book (all fields)

```graphql
mutation {
  updateBook(
    id: 1
    title: "Updated Book"
    authorId: 1
    publishingCompanyId: 1
    publishingYear: 2025
  ) {
    id
    title
    publishingYear
  }
}
```

## Delete a book

```graphql
mutation {
  deleteBook(id: 1)
}
```

## Create / update / delete an author

```graphql
mutation {
  createAuthor(name: "John", surname: "Doe") {
    id
    name
    surname
  }
}
```

```graphql
mutation {
  updateAuthor(id: 1, name: "Jane", surname: "Doe") {
    id
    name
    surname
  }
}
```

```graphql
mutation {
  deleteAuthor(id: 1)
}
```

## Create / update / delete a publisher

```graphql
mutation {
  createPublisher(name: "Example Publishing") {
    id
    name
  }
}
```

```graphql
mutation {
  updatePublisher(id: 1, name: "Updated Publishing") {
    id
    name
  }
}
```

```graphql
mutation {
  deletePublisher(id: 1)
}
```

---

# Validation and error handling

Every expected failure is returned as a GraphQL error with a machine-readable
`extensions.code`:

| Code | Meaning | Mirrors |
|---|---|---|
| `VALIDATION_ERROR` | Missing/invalid value, non-existing author/publisher, publishing year < 1900 | SOAP `ValidationFault` |
| `NOT_FOUND` | The requested ID does not exist | SOAP `NotFoundFault` |
| `CONFLICT` | Deleting an author/publisher that is still referenced by a book | SOAP `ConflictFault` |

Example error response:

```json
{
  "errors": [
    {
      "message": "Field \"publishingYear\" must be an integer greater than or equal to 1900.",
      "extensions": { "code": "VALIDATION_ERROR" }
    }
  ]
}
```

The rules enforced by the API are:

- `title` / `name` must be a non-empty string.
- `publishingYear` must be an integer `>= 1900`.
- A book must reference an **existing** author and an **existing** publisher.
- An author or publisher **cannot be deleted** while books reference it.

---

# Testing

## Automated smoke test

A self-contained smoke test starts the server on a temporary **copy** of the
database (the real `library.db` is never modified), runs every query and
mutation, and checks the expected results, including the error cases:

```bash
npm run smoke
```

Expected output ends with:

```text
22 passed, 0 failed
```

## Manual testing

Open `http://localhost:4000/graphql` in a browser to use the built-in
**GraphiQL** IDE, or send POST requests from Postman / Insomnia / any GraphQL
client:

```text
POST http://localhost:4000/graphql
Content-Type: application/json

{ "query": "{ authors { id name surname } }" }
```

---

# Postman collection

A ready-to-import collection and environment are included in `graphql/postman/`:

```text
graphql/postman/
├── Library GraphQL API.postman_collection.json
└── Library GraphQL Environment.postman_environment.json
```

Import both into Postman, select the **Library GraphQL Environment**, and run the
requests. The collection contains:

- **Schema** — Schema Introspection (returns the full schema)
- **Queries** — Get Book by ID, List Authors, List Publishers, List Publishers with Authors and Books
- **Book Mutations** — Create / Update / Delete
- **Author Mutations** — Create / Update / Delete
- **Publisher Mutations** — Create / Update / Delete
- **Error Handling** — not found, validation, and delete-conflict cases
- **Advantages (notes.md)** — one request per GraphQL advantage (granularity, nesting,
  fragments, one round trip, unions, no versioning)
- **Disadvantages (notes.md)** — one request per GraphQL disadvantage (POST-only / caching,
  errors are HTTP 200, problematic nesting, strict schema, required variables)

The create requests save the new ID into the `bookId` / `authorId` /
`publisherId` environment variables, so the update and delete requests reuse the
same IDs automatically. The **Advantages** and **Disadvantages** folders use two
extra variables: `searchTerm` (default `Rowling`) and `demoBookId` (default
`1000`, a stable book that the mutation requests never touch).

---

# Project structure

```text
graphql/
├── src/
│   ├── index.js        # HTTP server + GraphQL endpoint (/graphql)
│   ├── schema.js       # GraphQL schema (SDL): types, queries, mutations
│   ├── resolvers.js    # Business logic + validation
│   ├── repository.js   # SQLite access (maps DB columns to GraphQL fields)
│   ├── db.js           # Database connection + schema creation
│   └── errors.js       # GraphQLError helpers (NOT_FOUND / VALIDATION_ERROR / CONFLICT)
├── scripts/
│   └── smoke-test.mjs  # Automated end-to-end smoke test
├── postman/
│   ├── Library GraphQL API.postman_collection.json
│   └── Library GraphQL Environment.postman_environment.json
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── package.json
├── GUIDE.md            # GraphQL learning guide (like the SOAP guide)
└── README.md
```

---

# Notes

- The database is **shared** with the other APIs. It is mounted read/write, so
  mutations made through GraphQL are visible to the REST, SOAP and gRPC APIs.
- SQLite column names (`nBookID`, `cTitle`, ...) are mapped to the GraphQL field
  names (`id`, `title`, ...) in `repository.js`, so the GraphQL layer stays clean.
- `node:sqlite` is used instead of a third-party driver, so there are no native
  build steps and the Docker image is small and fast to build.

---

## Related documentation

- [GraphQL Learning Guide](GUIDE.md)
- [Project README](../README.md)
- [REST API](../rest/README.md)
- [SOAP API](../soap/README.md)
- [gRPC API](../grpc/README.md)
- [API Testing](../postman/README.md)
