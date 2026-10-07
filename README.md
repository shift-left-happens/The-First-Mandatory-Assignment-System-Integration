# Library API Integration Project

Software Integration — First Mandatory Assignment

A library management system exposing the same underlying library data through four different API technologies:

- **REST**
- **SOAP**
- **GraphQL**
- **gRPC**

The project is developed as part of the Software Integration course and demonstrates how different API paradigms can expose and manipulate the same domain model.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Domain Model](#domain-model)
- [APIs](#apis)
  - [REST API](#rest-api)
  - [SOAP API](#soap-api)
  - [GraphQL API](#graphql-api)
  - [gRPC API](#grpc-api)
- [Repository Structure](#repository-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Database](#database)
- [API Testing](#api-testing)
- [Error Handling](#error-handling)
- [gRPC Book Notifications](#grpc-book-notifications)
- [Design Considerations](#design-considerations)
- [Assignment Requirements Checklist](#assignment-requirements-checklist)
- [Presentation](#presentation)
- [Team](#team)

---

## Project Overview

The goal of this project is to implement a library system using four different API technologies.

All four APIs operate on the same underlying library data and provide access to books, authors, and publishing companies.

The project demonstrates the differences between:

| API | Purpose |
|---|---|
| REST | Resource-oriented HTTP API |
| SOAP | Contract-based XML web service |
| GraphQL | Query and mutation API |
| gRPC | High-performance RPC API with streaming |

The APIs share the same domain and database so that their capabilities and trade-offs can be compared directly.

---

## Architecture

The project follows a shared-domain architecture where the four APIs expose different interfaces over the same underlying application/data layer.

```text
                         ┌─────────────────┐
                         │     Client      │
                         └────────┬────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
              ▼                   ▼                   ▼
        ┌───────────┐       ┌───────────┐       ┌───────────┐
        │ REST API  │       │ SOAP API  │       │ GraphQL   │
        └─────┬─────┘       └─────┬─────┘       │   API     │
              │                   │             └─────┬─────┘
              │                   │                   │
              └───────────────────┼───────────────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Application /   │
                         │ Domain Layer    │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │    Database     │
                         └─────────────────┘

                         ┌─────────────────┐
                         │    gRPC API     │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Application /   │
                         │ Domain Layer    │
                         └─────────────────┘
```

The exact implementation and project structure may vary depending on the technologies used.

---

## Domain Model

The library consists of three primary entities:

### Book

| Field | Type | Description |
|---|---|---|
| `id` | Integer | Unique book identifier |
| `title` | String | Book title |
| `authorId` | Integer | Reference to an author |
| `publishingCompanyId` | Integer | Reference to a publishing company |
| `publishingYear` | Integer | Publication year, minimum 1900 |

### Author

| Field | Type | Description |
|---|---|---|
| `id` | Integer | Unique author identifier |
| `name` | String | Author's first name |
| `surname` | String | Author's surname |

### Publishing Company

| Field | Type | Description |
|---|---|---|
| `id` | Integer | Unique publishing company identifier |
| `name` | String | Publishing company name |

### Relationships

```text
Author
  │
  │ 1:N
  ▼
Book
  ▲
  │ N:1
  │
Publishing Company
```

A book must reference an existing author and publishing company.

---

# APIs

## REST API

The REST API exposes the library as HTTP resources.

### Books

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/books` | Create a book |
| `GET` | `/books/{id}` | Get a book by ID |
| `GET` | `/books` | List books with pagination |
| `PUT/PATCH` | `/books/{id}` | Update a book |
| `DELETE` | `/books/{id}` | Delete a book |

Book listing supports pagination using:

```text
GET /books?limit=20&offset=0
```

### Authors

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/authors` | Create an author |
| `GET` | `/authors/{id}` | Get an author by ID |
| `GET` | `/authors` | List all authors |
| `PUT/PATCH` | `/authors/{id}` | Update an author |
| `DELETE` | `/authors/{id}` | Delete an author |

### Publishers

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/publishers` | Create a publisher |
| `GET` | `/publishers/{id}` | Get a publisher by ID |
| `GET` | `/publishers` | List all publishers |
| `PUT/PATCH` | `/publishers/{id}` | Update a publisher |
| `DELETE` | `/publishers/{id}` | Delete a publisher |

---

## SOAP API

The SOAP API exposes the library through an HTTP SOAP endpoint.

The service defines XML Schema (XSD) complex types for:

- `Book`
- `Author`
- `PublishingCompany`

### Book

```text
Book
├── id: integer
├── title: string
├── authorId: integer
├── publishingCompanyId: integer
└── publishingYear: integer >= 1900
```

### Author

```text
Author
├── id: integer
├── name: string
└── surname: string
```

### Publishing Company

```text
PublishingCompany
├── id: integer
└── name: string
```

### Book Operations

| Operation | Input | Output |
|---|---|---|
| `CreateBook` | title, authorId, publishingCompanyId, publishingYear | Generated ID |
| `GetBookById` | id | Book |
| `UpdateBook` | id + book fields | Success acknowledgement |
| `DeleteBook` | id | Success acknowledgement |

### Author Operations

| Operation | Input | Output |
|---|---|---|
| `CreateAuthor` | name, surname | Generated ID |
| `GetAuthorById` | id | Author |
| `ListAuthors` | None | Array of Author |
| `UpdateAuthor` | id + author fields | Success acknowledgement |
| `DeleteAuthor` | id | Success acknowledgement |

### Publishing Company Operations

| Operation | Input | Output |
|---|---|---|
| `CreatePublishingCompany` | name | Generated ID |
| `GetPublishingCompanyById` | id | PublishingCompany |
| `ListPublishingCompanies` | None | Array of PublishingCompany |
| `UpdatePublishingCompany` | id + name | Success acknowledgement |
| `DeletePublishingCompany` | id | Success acknowledgement |

### SOAP Faults

The SOAP API uses the following faults:

#### `NotFoundFault`

Returned when the requested entity does not exist.

#### `ValidationFault`

Returned when:

- Required fields are missing
- Field values are invalid
- A referenced author does not exist
- A referenced publishing company does not exist
- `publishingYear` is less than `1900`

#### `ConflictFault`

Returned when attempting to delete an entity that is referenced by another entity.

For example, an author cannot be deleted while one or more books reference that author.

---

# GraphQL API

The GraphQL API is exposed through:

```text
POST /graphql
```

The API defines the following main types:

- `Book`
- `Author`
- `Publisher`

## Queries

### Get book by ID

```graphql
query {
  book(id: 1) {
    id
    title
    authorId
    publishingCompanyId
    publishingYear
  }
}
```

### List authors

```graphql
query {
  authors {
    id
    name
    surname
  }
}
```

### List publishers

```graphql
query {
  publishers {
    id
    name
  }
}
```

## Mutations

The GraphQL API supports:

### Books

- Create book
- Update book by ID
- Delete book

### Authors

- Create author
- Update author by ID
- Delete author

### Publishers

- Create publisher
- Update publisher by ID
- Delete publisher

Example mutation:

```graphql
mutation {
  createBook(
    title: "Example Book"
    authorId: 1
    publisherId: 1
    publishingYear: 2024
  ) {
    id
  }
}
```

The exact GraphQL schema and field naming conventions are defined in the implementation.

---

# gRPC API

The gRPC API exposes a service defined using Protocol Buffers.

The service provides three RPC methods.

## GetBookById

Unary RPC:

```text
GetBookById(GetBookByIdRequest) returns (Book)
```

Input:

- Book ID

Returns:

- Book

---

## CreateBook

Unary RPC:

```text
CreateBook(CreateBookRequest) returns (CreateBookResponse)
```

Input:

- Book name/title
- Author ID
- Publisher ID
- Publication year

Returns:

- Generated book ID

---

## WatchBooks

Server-streaming RPC:

```text
WatchBooks(WatchBooksRequest) returns (stream Book)
```

Clients can subscribe to the `WatchBooks` stream.

Whenever another client creates a new book, subscribed clients receive the newly created book through the stream.

Conceptually:

```text
Client A                         gRPC Server
   │                                  │
   │──── WatchBooks ─────────────────>│
   │                                  │
   │                                  │
Client B                              │
   │──── CreateBook ─────────────────>│
   │                                  │
   │                                  │
   │<──── New Book ───────────────────│
   │                                  │
```

This demonstrates server-side streaming and real-time event delivery using gRPC.

---

# Repository Structure

The repository is organized into separate components for each API.

```text
.
├── README.md
│
├── rest/
│   └── ...
│
├── soap/
│   └── ...
│
├── graphql/
│   └── ...
│
├── grpc/
│   └── ...
│
├── database/
│   ├── schema.sql
│   └── seed.sql
│
├── tests/
│   └── ...
│
└── postman/
    ├── collection.json
    └── environment.json
```

> Update this section to match the actual repository structure.

---

# Prerequisites

Before running the project, install the required development tools.

Depending on the implementation, this may include:

- [Runtime / SDK]
- [Package manager]
- SQLite
- Docker (if applicable)
- Postman, Insomnia, or another API testing tool
- Protocol Buffers / `protoc` for gRPC development

> Replace this section with the exact versions and tools required by the project.

Example:

```text
Runtime: .NET 8
Database: SQLite
API testing: Postman
```

---

# Getting Started

## 1. Clone the repository

```bash
git clone <repository-url>
cd <repository-directory>
```

## 2. Configure the application

Copy the example environment/configuration file if applicable:

```bash
cp .env.example .env
```

Configure the database connection and other required settings.

## 3. Initialize the database

If the project uses the supplied SQLite database, place it in the appropriate location.

If the database has been migrated to another DBMS, run the provided database scripts:

```bash
# Example
database/schema.sql
database/seed.sql
```

## 4. Start the APIs

Start each API according to the instructions in its respective directory.

Example:

```bash
cd rest
# start REST API
```

```bash
cd soap
# start SOAP API
```

```bash
cd graphql
# start GraphQL API
```

```bash
cd grpc
# start gRPC API
```

> Replace the commands above with the actual commands used by the project.

---

# Database

The APIs use a shared library database containing:

- Books
- Authors
- Publishing Companies

The original assignment provides a SQLite database.

If the project uses a different DBMS, the required migration/database scripts are included in:

```text
/database
```

The database maintains the following important constraints:

- Every book must reference an existing author.
- Every book must reference an existing publishing company.
- Publishing years must be `>= 1900`.
- Authors cannot be deleted while referenced by books.
- Publishing companies cannot be deleted while referenced by books.

---

# API Testing

API testing material is included in the repository.

The preferred testing tool for this project is:

**[Postman / Insomnia / Thunder Client]**

> Update this to the actual tool used.

Testing collections/environments can be found in:

```text
/postman
```

The test material covers all four APIs:

- REST
- SOAP
- GraphQL
- gRPC

At minimum, the testing demonstrates:

- Creating entities
- Retrieving entities
- Updating entities
- Deleting entities
- Validation failures
- Not-found scenarios
- Conflict scenarios where applicable
- REST pagination
- GraphQL queries and mutations
- gRPC streaming through `WatchBooks`

---

# Error Handling

The APIs validate input and maintain the integrity of relationships between entities.

Common error scenarios include:

### Entity not found

The requested entity ID does not exist.

Example:

```text
GET /books/999999
```

Expected result: a not-found response.

### Invalid data

Examples:

- Missing required fields
- Invalid IDs
- Invalid publishing year
- Non-existing author
- Non-existing publishing company

### Delete conflict

An author or publishing company cannot be deleted if books still reference it.

Example:

```text
DELETE /authors/1
```

If books reference author `1`, the operation must fail with a conflict.

---

# Design Considerations

This project intentionally implements the same library domain using four different API styles.

## REST

### Advantages

- Simple and widely understood
- Works naturally with HTTP
- Easy to test and integrate
- Good browser/tooling support
- Resource-oriented architecture

### Disadvantages

- Clients may receive more or less data than required
- Multiple requests may be necessary for related data
- API versioning can become challenging

---

## SOAP

### Advantages

- Strong contracts through WSDL/XSD
- Built-in standards for structured messaging and faults
- Suitable for enterprise integrations
- Strongly typed XML contracts

### Disadvantages

- Verbose XML messages
- More complex than REST for simple APIs
- More difficult to consume from modern frontend applications
- Higher implementation overhead

---

## GraphQL

### Advantages

- Clients can request exactly the fields they need
- Flexible querying
- Multiple related resources can be retrieved in a single request
- Strongly typed schema

### Disadvantages

- More complex server implementation
- Caching is less straightforward than traditional HTTP caching
- Poorly designed queries can be expensive
- Requires additional consideration for authorization and query complexity

---

## gRPC

### Advantages

- High performance
- Efficient binary serialization using Protocol Buffers
- Strongly typed contracts
- Built-in streaming support
- Well suited for service-to-service communication

### Disadvantages

- Less convenient for browser-based clients
- Requires generated client/server code
- Less human-readable than REST/JSON
- Additional infrastructure/tooling is required

---

# Assignment Requirements Checklist

## REST API

- [ ] Books — Create
- [ ] Books — Get by ID
- [ ] Books — List with pagination
- [ ] Books — Update
- [ ] Books — Delete
- [ ] Authors — Create
- [ ] Authors — Get by ID
- [ ] Authors — List all
- [ ] Authors — Update
- [ ] Authors — Delete
- [ ] Publishers — Create
- [ ] Publishers — Get by ID
- [ ] Publishers — List all
- [ ] Publishers — Update
- [ ] Publishers — Delete

## SOAP API

- [ ] Book XSD type
- [ ] Author XSD type
- [ ] PublishingCompany XSD type
- [ ] CreateBook
- [ ] GetBookById
- [ ] UpdateBook
- [ ] DeleteBook
- [ ] CreateAuthor
- [ ] GetAuthorById
- [ ] ListAuthors
- [ ] UpdateAuthor
- [ ] DeleteAuthor
- [ ] CreatePublishingCompany
- [ ] GetPublishingCompanyById
- [ ] ListPublishingCompanies
- [ ] UpdatePublishingCompany
- [ ] DeletePublishingCompany
- [ ] NotFoundFault
- [ ] ValidationFault
- [ ] ConflictFault

## GraphQL API

- [ ] `/graphql` POST endpoint
- [ ] Book type
- [ ] Author type
- [ ] Publisher type
- [ ] Get book by ID
- [ ] List authors
- [ ] List publishers
- [ ] Create book
- [ ] Update book
- [ ] Delete book
- [ ] Create author
- [ ] Update author
- [ ] Delete author
- [ ] Create publisher
- [ ] Update publisher
- [ ] Delete publisher

## gRPC API

- [ ] `GetBookById`
- [ ] `CreateBook`
- [ ] `WatchBooks`
- [ ] Server streaming implemented
- [ ] Subscribers receive newly created books
- [ ] Protocol Buffer definitions included

## Delivery

- [ ] Source code for all four APIs
- [ ] Database / migration script
- [ ] API testing collections
- [ ] API testing environments
- [ ] README documentation
- [ ] Final ZIP package

---

# Presentation

The project will be presented on **22 October 2026**.

The presentation must demonstrate:

1. All four APIs using an API testing tool.
2. The implementation and architecture.
3. The relevant source code.
4. Advantages and disadvantages of each API approach for this library scenario.

The presentation is limited to **12 minutes per group**, and every group member must present part of the project.

The presentation should be based on the delivered project. No additional presentation media is required.

---

# Team

| Name | Responsibility |
|---|---|
| Student 1 | |
| Student 2 | |
| Student 3 | |
| Student 4 | |
| Student 5 | |

---

# Submission

The final submission consists of one ZIP file containing:

```text
Project/
├── REST API source code
├── SOAP API source code
├── GraphQL API source code
├── gRPC API source code
├── Database / migration scripts
├── API testing collections
├── API testing environments
└── README.md
```

**Submission deadline:** 21 October 2026 at 23:59.

---

## License

This project was developed as part of the Software Integration course and is intended for educational purposes.