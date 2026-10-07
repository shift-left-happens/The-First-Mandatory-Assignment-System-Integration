# API Testing

This directory contains the API testing collections and environments used to demonstrate the four library APIs.

The preferred testing tool for this project is **Postman**.

---

# APIs

The collection contains requests for:

- REST
- SOAP
- GraphQL
- gRPC

---

# Directory Structure

```text
postman/
├── README.md
├── Library APIs.postman_collection.json
└── Library APIs.postman_environment.json
```

> Rename the files above if the actual filenames differ.

---

# Environment

The Postman environment contains the URLs and configuration required to run the APIs locally.

Example variables:

| Variable | Example |
|---|---|
| `restUrl` | `http://localhost:5001` |
| `soapUrl` | `http://localhost:5002` |
| `graphqlUrl` | `http://localhost:5003/graphql` |
| `grpcHost` | `localhost` |
| `grpcPort` | `5004` |

> Update the values according to the actual project configuration.

---

# REST Collection

The REST collection demonstrates:

### Books

- Create
- Get by ID
- List
- Pagination
- Update
- Delete

### Authors

- Create
- Get by ID
- List
- Update
- Delete

### Publishers

- Create
- Get by ID
- List
- Update
- Delete

### Error Cases

- Invalid request
- Non-existing entity
- Invalid foreign key
- Delete conflict

---

# SOAP Collection

The SOAP collection demonstrates:

- `CreateBook`
- `GetBookById`
- `UpdateBook`
- `DeleteBook`
- `CreateAuthor`
- `GetAuthorById`
- `ListAuthors`
- `UpdateAuthor`
- `DeleteAuthor`
- `CreatePublishingCompany`
- `GetPublishingCompanyById`
- `ListPublishingCompanies`
- `UpdatePublishingCompany`
- `DeletePublishingCompany`

The collection also demonstrates:

- `ValidationFault`
- `NotFoundFault`
- `ConflictFault`

---

# GraphQL Collection

The GraphQL collection demonstrates:

### Queries

- Get book by ID
- List authors
- List publishers

### Mutations

- Create book
- Update book
- Delete book
- Create author
- Update author
- Delete author
- Create publisher
- Update publisher
- Delete publisher

---

# gRPC Collection

The gRPC collection demonstrates:

- `GetBookById`
- `CreateBook`
- `WatchBooks`

For `WatchBooks`, the demonstration should use at least two clients:

```text
Client A
   │
   └── WatchBooks
          │
          ▼
       Server
          ▲
          │
   CreateBook
   Client B
```

After Client B creates a book, Client A should receive the newly created book through the stream.

---

# Importing the Collection

1. Open Postman.
2. Select **Import**.
3. Import the collection file.
4. Import the environment file.
5. Select the imported environment.
6. Start the required API services.
7. Run the requests.

---

# Presentation Demo

For the final presentation, the collection should be prepared so that the complete API demonstration can be performed quickly.

Recommended order:

1. REST CRUD
2. SOAP operations and faults
3. GraphQL queries/mutations
4. gRPC unary operations
5. gRPC `WatchBooks` streaming

The gRPC streaming demo should ideally be prepared beforehand so the behavior can be demonstrated within the 12-minute presentation limit.

---

## Related Documentation

- [Project README](../README.md)
- [REST API](../rest/README.md)
- [SOAP API](../soap/README.md)
- [GraphQL API](../graphql/README.md)
- [gRPC API](../grpc/README.md)