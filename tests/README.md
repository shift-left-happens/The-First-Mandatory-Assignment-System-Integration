# Tests

This directory contains automated and/or integration tests for the library APIs.

The purpose of the tests is to verify that the APIs behave correctly against the shared library domain and database.

---

# Test Coverage

Tests should cover the functionality required by the assignment.

## REST

- [ ] Create book
- [ ] Get book by ID
- [ ] List books
- [ ] Pagination
- [ ] Update book
- [ ] Delete book
- [ ] Author CRUD
- [ ] Publishing company CRUD
- [ ] Validation
- [ ] Not found
- [ ] Delete conflicts

## SOAP

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
- [ ] ValidationFault
- [ ] NotFoundFault
- [ ] ConflictFault

## GraphQL

- [ ] Get book
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

## gRPC

- [ ] GetBookById
- [ ] CreateBook
- [ ] WatchBooks
- [ ] Streaming notifications
- [ ] Validation
- [ ] Not-found scenarios

---

# Running Tests

> Replace the commands below with the actual test commands for the chosen technology.

```bash
# Run all tests
<test-command>
```

For a specific API:

```bash
<REST-test-command>
<SOAP-test-command>
<GraphQL-test-command>
<gRPC-test-command>
```

---

# Integration Testing

Integration tests should verify that the APIs correctly interact with the database.

Important scenarios include:

1. Create an author.
2. Create a publisher.
3. Create a book referencing both.
4. Retrieve the book.
5. Update the book.
6. Delete the book.
7. Delete the author/publisher.

The tests should also verify that deleting an author or publisher that is still referenced by a book is rejected.

---

# Test Isolation

Tests should avoid depending on the state produced by other tests.

Where possible:

- Use a dedicated test database.
- Reset database state between test suites.
- Use deterministic test data.
- Avoid relying on auto-generated IDs unless necessary.

---

## Related Documentation

- [Project README](../README.md)
- [REST API](../rest/README.md)
- [SOAP API](../soap/README.md)
- [GraphQL API](../graphql/README.md)
- [gRPC API](../grpc/README.md)