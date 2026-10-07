# GraphQL API

The GraphQL API provides a flexible query and mutation interface for the library system.

The API is exposed through a single HTTP endpoint:

```text
POST /graphql
```

---

# Endpoint

```text
http://localhost:<PORT>/graphql
```

> Update the port according to the implementation.

GraphQL requests are sent using HTTP POST.

---

# Types

The API defines the following main types:

- `Book`
- `Author`
- `Publisher`

---

## Book

```graphql
type Book {
  id: Int!
  title: String!
  authorId: Int!
  publishingCompanyId: Int!
  publishingYear: Int!
}
```

---

## Author

```graphql
type Author {
  id: Int!
  name: String!
  surname: String!
}
```

---

## Publisher

```graphql
type Publisher {
  id: Int!
  name: String!
}
```

---

# Queries

## Get Book by ID

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

---

## List Authors

```graphql
query {
  authors {
    id
    name
    surname
  }
}
```

---

## List Publishers

```graphql
query {
  publishers {
    id
    name
  }
}
```

---

# Mutations

## Create Book

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

---

## Update Book

Updates all book fields.

```graphql
mutation {
  updateBook(
    id: 1
    title: "Updated Book"
    authorId: 1
    publisherId: 1
    publishingYear: 2025
  ) {
    id
    title
    authorId
    publishingCompanyId
    publishingYear
  }
}
```

---

## Delete Book

```graphql
mutation {
  deleteBook(id: 1)
}
```

---

## Create Author

```graphql
mutation {
  createAuthor(
    name: "John"
    surname: "Doe"
  ) {
    id
  }
}
```

---

## Update Author

```graphql
mutation {
  updateAuthor(
    id: 1
    name: "Jane"
    surname: "Doe"
  ) {
    id
    name
    surname
  }
}
```

---

## Delete Author

```graphql
mutation {
  deleteAuthor(id: 1)
}
```

---

## Create Publisher

```graphql
mutation {
  createPublisher(
    name: "Example Publishing"
  ) {
    id
  }
}
```

---

## Update Publisher

```graphql
mutation {
  updatePublisher(
    id: 1
    name: "Updated Publishing"
  ) {
    id
    name
  }
}
```

---

## Delete Publisher

```graphql
mutation {
  deletePublisher(id: 1)
}
```

> The exact mutation names and return types should match the implemented schema.

---

# Validation

The API validates:

- Required fields
- Entity IDs
- Author references
- Publisher references
- Publishing year

Publishing years must be at least:

```text
1900
```

---

# Running the API

> Update the commands according to the implementation.

```bash
# Install dependencies
<install-command>

# Start GraphQL API
<run-command>
```

The GraphQL endpoint will then be available at:

```text
http://localhost:<PORT>/graphql
```

If the implementation provides a GraphQL IDE such as GraphiQL or Apollo Sandbox, it can be accessed through the corresponding development URL.

---

# Testing

GraphQL requests can be tested using:

- Postman
- Insomnia
- GraphiQL
- Apollo Sandbox
- Another GraphQL-compatible client

The test suite should demonstrate:

- Queries
- Mutations
- Successful CRUD operations
- Invalid references
- Invalid publishing years
- Non-existing IDs
- Delete conflicts

---

# Why GraphQL?

GraphQL allows clients to specify exactly which fields they require.

For example:

```graphql
query {
  authors {
    name
  }
}
```

The client only requests the `name` field rather than receiving the complete author object.

This is particularly useful when clients have different data requirements.

---

## Related Documentation

- [Project README](../README.md)
- [Database README](../database/README.md)
- [API Testing](../tests/README.md)