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
http://localhost:5080/LibraryService.svc
```

The generated or exposed WSDL can be accessed through:

```text
http://localhost:5080/LibraryService.svc?wsdl         (WSDL with imported XSDs)
http://localhost:5080/LibraryService.svc?singleWsdl   (single flattened WSDL, includes the XSD types)
```

Namespace: `http://library.example/soap` &middot; SOAP 1.1 (`BasicHttpBinding`), document/literal.

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

Implemented in C# on **.NET 10** with **CoreWCF** (SOAP/WSDL), **Dapper** and **SQLite**.

```bash
cd soap
dotnet run --project LibrarySoap
```

The service listens on `http://localhost:5080`.

**Database:** on first start the shared `database/library.db` is copied to
`LibrarySoap/library.dev.db` (gitignored), and the service only ever reads and writes that copy.
You can test freely without changing the tracked database. To reset to the original data,
stop the service and delete `library.dev.db`. Paths are set in `LibrarySoap/appsettings.json`
(`Database:Source`, `Database:WorkingCopy`).

## Project layout

Organised by feature. All features add to **one** SOAP contract (`partial` interface/class),
so there is still one endpoint and one WSDL.

```text
soap/
├── GUIDE.md                         learning guide: SOAP concepts mapped to this code
├── LibrarySoap.slnx
├── LibrarySoap/
│   ├── Program.cs                   host setup, endpoint, WSDL publishing
│   ├── ILibraryService.cs           [ServiceContract], the root of the contract
│   ├── LibraryService.cs            implementation root (constructor + repositories)
│   ├── Shared/                      namespace, faults, validation, DB connection
│   └── Features/
│       ├── Books/                   Book, contract ops, implementation, repository
│       ├── Authors/                 Author, ...
│       └── PublishingCompanies/     PublishingCompany, ...
└── postman/                         collection + environment
```

New to SOAP? Start with [GUIDE.md](GUIDE.md).

## Request format

Every operation is an HTTP `POST` to the endpoint with:

```text
Content-Type: text/xml; charset=utf-8
SOAPAction: "http://library.example/soap/LibraryService/<Operation>"
```

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Body>
    <GetBookById xmlns="http://library.example/soap"><id>1000</id></GetBookById>
  </soapenv:Body>
</soapenv:Envelope>
```

Faults come back as HTTP `500` with a SOAP `<Fault>` whose `<detail>` holds `NotFoundFault`,
`ValidationFault` or `ConflictFault`.

> Known limitation: the code-first contract cannot express `publishingYear >= 1900` as an XSD
> restriction, so that rule is enforced at runtime and reported as a `ValidationFault`.

---

# Testing

The SOAP service can be tested using:

- Postman
- SoapUI
- Insomnia
- Another SOAP-compatible client

A ready-made Postman collection is in [`postman/`](postman/): import
`Library-SOAP.postman_collection.json` and `Library-SOAP.postman_environment.json`, start the
service and run the collection (it creates its own test data and deletes it again). From the
command line:

```bash
npx newman run postman/Library-SOAP.postman_collection.json -e postman/Library-SOAP.postman_environment.json
```

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