# gRPC API

The gRPC API provides a strongly typed RPC interface for the library system.

Unlike the REST, SOAP, and GraphQL APIs, the gRPC API also demonstrates server-side streaming.

The service is defined using Protocol Buffers (`.proto`).

---

# Service

The gRPC service provides three RPC methods:

```text
GetBookById
CreateBook
WatchBooks
```

---

# Protocol Buffers

The service contract is defined in the project's `.proto` file.

Example structure:

```protobuf
service LibraryService {
  rpc GetBookById(GetBookByIdRequest) returns (Book);

  rpc CreateBook(CreateBookRequest) returns (CreateBookResponse);

  rpc WatchBooks(WatchBooksRequest) returns (stream Book);
}
```

The exact package and message names should match the implementation.

---

# RPC Methods

## GetBookById

Unary RPC:

```protobuf
rpc GetBookById(GetBookByIdRequest) returns (Book);
```

### Request

```protobuf
message GetBookByIdRequest {
  int32 id = 1;
}
```

### Response

Returns a `Book`.

Example:

```text
GetBookById
      │
      ▼
┌──────────────┐
│ book ID = 1  │
└──────┬───────┘
       │
       ▼
    Server
       │
       ▼
    Book
```

---

# CreateBook

Unary RPC:

```protobuf
rpc CreateBook(CreateBookRequest) returns (CreateBookResponse);
```

The request contains:

- Book title/name
- Author ID
- Publisher ID
- Publishing year

The response contains the generated book ID.

Example:

```text
Client
  │
  │ CreateBook
  ▼
gRPC Server
  │
  ├── Validate author
  ├── Validate publisher
  ├── Validate year
  ├── Create book
  │
  ▼
CreateBookResponse
```

---

# WatchBooks

Server-streaming RPC:

```protobuf
rpc WatchBooks(WatchBooksRequest) returns (stream Book);
```

Clients can subscribe to this stream to receive newly created books.

Whenever any client successfully creates a book, the server sends the new `Book` to all subscribed clients.

---

## Streaming Example

```text
Client A                    Server                    Client B
   │                           │                         │
   │── WatchBooks ────────────>│                         │
   │                           │<──── WatchBooks ────────│
   │                           │                         │
   │                           │                         │
   │                           │<── CreateBook ──────────│
   │                           │                         │
   │<──── New Book ────────────│                         │
   │                           │──── New Book ──────────>│
   │                           │                         │
```

This demonstrates server-side event streaming using gRPC.

---

# Book Model

The gRPC `Book` message represents a library book.

Conceptually:

```protobuf
message Book {
  int32 id = 1;
  string title = 2;
  int32 author_id = 3;
  int32 publisher_id = 4;
  int32 publishing_year = 5;
}
```

The exact field names and numbering are defined by the project's `.proto` file.

---

# Validation

Book creation validates:

- Title/name
- Author ID
- Publisher ID
- Publishing year

The referenced author and publisher must exist.

The publishing year must be:

```text
>= 1900
```

---

# Running the API

> Update the commands according to the implementation.

```bash
# Install dependencies
<install-command>

# Start gRPC server
<run-command>
```

The gRPC server will listen on:

```text
localhost:<PORT>
```

---

# Testing

gRPC can be tested using tools such as:

- Postman
- Kreya
- grpcurl
- BloomRPC
- Custom gRPC client

Testing should cover:

### GetBookById

- Existing book
- Non-existing book

### CreateBook

- Valid book
- Invalid author
- Invalid publisher
- Invalid publishing year

### WatchBooks

1. Start a `WatchBooks` subscription.
2. Create a book using another client.
3. Verify that the subscribed client receives the new book.

---

# Streaming Requirements

The important requirement for `WatchBooks` is that the stream remains active while the client is subscribed.

When another client creates a book:

```text
CreateBook
    │
    ▼
Database
    │
    ▼
BookCreated event
    │
    ▼
Connected WatchBooks clients
    │
    ├── Client A
    ├── Client B
    └── Client C
```

Each subscribed client receives the newly created book.

---

# Why gRPC?

gRPC uses Protocol Buffers for compact binary serialization and strongly typed service contracts.

It is particularly useful for:

- Service-to-service communication
- High-performance APIs
- Strongly typed contracts
- Streaming
- Internal microservice communication

For this assignment, the `WatchBooks` operation demonstrates a capability that is particularly natural in gRPC.

---

## Related Documentation

- [Project README](../README.md)
- [Database README](../database/README.md)
- [API Testing](../tests/README.md)