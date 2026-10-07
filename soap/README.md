# SOAP API

The SOAP API exposes the library system through a contract-based XML web service.

The service provides operations for:

- Books
- Authors
- Publishing Companies

The API uses SOAP messages and XML Schema (XSD) types.

---

## Service

The SOAP service is exposed through an HTTP endpoint.

```text
http://localhost:<PORT>/<SERVICE_PATH>
```

The generated or exposed WSDL can be accessed through:

```text
http://localhost:<PORT>/<SERVICE_PATH>?wsdl
```

> Update the URLs above according to the implementation.

---

## Data Types

### Book

```text
Book
├── id: integer
├── title: string
├── authorId: integer
├── publishingCompanyId: integer
└── publishingYear: integer
```

Constraints:

- `authorId` must reference an existing author.
- `publishingCompanyId` must reference an existing publishing company.
- `publishingYear >= 1900`.

---

### Author

```text
Author
├── id: integer
├── name: string
└── surname: string
```

---

### PublishingCompany

```text
PublishingCompany
├── id: integer
└── name: string
```

---

# Operations

## Book Operations

### CreateBook

Creates a new book.

**Input:**

- `title`
- `authorId`
- `publishingCompanyId`
- `publishingYear`

**Output:**

- Generated book ID

**Faults:**

- `ValidationFault`

---

### GetBookById

Retrieves a book by ID.

**Input:**

- `id`

**Output:**

- `Book`

**Faults:**

- `NotFoundFault`

---

### UpdateBook

Updates an existing book.

**Input:**

- `id`
- Updated book fields

**Output:**

- Success acknowledgement

**Faults:**

- `ValidationFault`
- `NotFoundFault`

---

### DeleteBook

Deletes a book.

**Input:**

- `id`

**Output:**

- Success acknowledgement

**Faults:**

- `NotFoundFault`

---

## Author Operations

### CreateAuthor

Creates a new author.

**Input:**

- `name`
- `surname`

**Output:**

- Generated author ID

**Faults:**

- `ValidationFault`

---

### GetAuthorById

Retrieves an author by ID.

**Input:**

- `id`

**Output:**

- `Author`

**Faults:**

- `NotFoundFault`

---

### ListAuthors

Returns all authors.

**Input:**

- None

**Output:**

- Array of `Author`

**Faults:**

- None expected

---

### UpdateAuthor

Updates an existing author.

**Input:**

- `id`
- Updated author fields

**Output:**

- Success acknowledgement

**Faults:**

- `ValidationFault`
- `NotFoundFault`

---

### DeleteAuthor

Deletes an author.

**Input:**

- `id`

**Output:**

- Success acknowledgement

**Faults:**

- `NotFoundFault`
- `ConflictFault`

An author cannot be deleted while books reference that author.

---

## Publishing Company Operations

### CreatePublishingCompany

Creates a new publishing company.

**Input:**

- `name`

**Output:**

- Generated publishing company ID

**Faults:**

- `ValidationFault`

---

### GetPublishingCompanyById

Retrieves a publishing company by ID.

**Input:**

- `id`

**Output:**

- `PublishingCompany`

**Faults:**

- `NotFoundFault`

---

### ListPublishingCompanies

Returns all publishing companies.

**Input:**

- None

**Output:**

- Array of `PublishingCompany`

**Faults:**

- None expected

---

### UpdatePublishingCompany

Updates an existing publishing company.

**Input:**

- `id`
- `name`

**Output:**

- Success acknowledgement

**Faults:**

- `ValidationFault`
- `NotFoundFault`

---

### DeletePublishingCompany

Deletes a publishing company.

**Input:**

- `id`

**Output:**

- Success acknowledgement

**Faults:**

- `NotFoundFault`
- `ConflictFault`

A publishing company cannot be deleted while books reference it.

---

# SOAP Faults

The service defines three application-level faults.

## NotFoundFault

Used when the requested entity does not exist.

Examples:

- Book ID does not exist
- Author ID does not exist
- Publishing company ID does not exist

---

## ValidationFault

Used when the request contains invalid data.

Examples:

- Missing required values
- Invalid IDs
- Non-existing author
- Non-existing publishing company
- Publishing year below `1900`

---

## ConflictFault

Used when deleting an entity that is referenced by another entity.

Example:

```text
DeleteAuthor(authorId)
        │
        ▼
Does a book reference this author?
        │
     ┌──┴──┐
    YES    NO
     │      │
     ▼      ▼
 Conflict  Delete
```

---

# XSD

The SOAP contract defines XML Schema types for:

- `Book`
- `Author`
- `PublishingCompany`

The XSD/WSDL definitions are included with the SOAP implementation.

---

# Running the API

> Update these commands according to the project's implementation.

```bash
# Install dependencies
<install-command>

# Start SOAP service
<run-command>
```

Once running, the WSDL should be available at:

```text
http://localhost:<PORT>/<SERVICE_PATH>?wsdl
```

---

# Testing

The SOAP service can be tested using:

- Postman
- SoapUI
- Insomnia
- Another SOAP-compatible client

Testing should cover:

- All successful operations
- Validation faults
- Not-found faults
- Conflict faults
- Foreign key validation
- Publishing year validation

---

## Related Documentation

- [Project README](../README.md)
- [Database README](../database/README.md)
- [API Testing](../tests/README.md)