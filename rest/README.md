# REST API

The REST API provides a resource-oriented HTTP interface for the library system.

It exposes CRUD operations for:

- Books
- Authors
- Publishing Companies

The API uses the shared library database.

---

## Responsibilities

The REST API is responsible for:

- Managing books
- Managing authors
- Managing publishing companies
- Validating incoming data
- Maintaining relationships between books, authors, and publishers
- Providing pagination for book listings
- Returning appropriate HTTP status codes

---

## Endpoints

### Books

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/books` | Create a book |
| `GET` | `/books/{id}` | Get a book by ID |
| `GET` | `/books` | List books |
| `PUT` | `/books/{id}` | Update a book |
| `DELETE` | `/books/{id}` | Delete a book |

### Pagination

Books support `limit` and `offset` query parameters.

```http
GET /books?limit=10&offset=0
```

Example:

```http
GET /books?limit=20&offset=40
```

This returns up to 20 books starting from offset 40.

---

### Authors

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/authors` | Create an author |
| `GET` | `/authors/{id}` | Get an author by ID |
| `GET` | `/authors` | List all authors |
| `PUT` | `/authors/{id}` | Update an author |
| `DELETE` | `/authors/{id}` | Delete an author |

---

### Publishing Companies

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/publishers` | Create a publishing company |
| `GET` | `/publishers/{id}` | Get a publishing company |
| `GET` | `/publishers` | List all publishing companies |
| `PUT` | `/publishers/{id}` | Update a publishing company |
| `DELETE` | `/publishers/{id}` | Delete a publishing company |

---

## Book Model

```json
{
  "id": 1,
  "title": "Example Book",
  "authorId": 1,
  "publishingCompanyId": 1,
  "publishingYear": 2024
}
```

A book must reference an existing author and publishing company.

The publishing year must be greater than or equal to `1900`.

---

## Author Model

```json
{
  "id": 1,
  "name": "John",
  "surname": "Doe"
}
```

---

## Publishing Company Model

```json
{
  "id": 1,
  "name": "Example Publishing"
}
```

---

## HTTP Status Codes

The API uses standard HTTP status codes.

| Status | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | Resource created successfully |
| `204 No Content` | Resource deleted successfully |
| `400 Bad Request` | Invalid request data |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Operation conflicts with existing relationships |
| `500 Internal Server Error` | Unexpected server error |

---

## Validation

The API validates:

- Required fields
- Entity IDs
- Foreign key relationships
- Publishing year

For example, creating a book with a non-existing author must fail.

```json
{
  "title": "Example Book",
  "authorId": 999999,
  "publishingCompanyId": 1,
  "publishingYear": 2024
}
```

---

## Running the API

> Update the commands below according to the project's implementation.

```bash
# Install dependencies
<install-command>

# Start the API
<run-command>
```

The API will be available at:

```text
http://localhost:<PORT>
```

---

## Testing

REST API requests can be found in the project's API testing collection.

See:

```text
../postman/
```

The test collection should cover:

- Create book
- Get book
- List books
- Pagination
- Update book
- Delete book
- Create author
- Get author
- List authors
- Update author
- Delete author
- Create publisher
- Get publisher
- List publishers
- Update publisher
- Delete publisher
- Validation errors
- Not-found errors
- Delete conflicts

---

## Related Documentation

- [Project README](../README.md)
- [Database README](../database/README.md)
- [API Testing](../tests/README.md)