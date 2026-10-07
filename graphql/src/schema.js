// GraphQL schema (SDL).
//
// The types and operations required by the assignment:
//   Types     : Book, Author, Publisher
//   Queries   : book, authors, publishers
//   Mutations : create/update/delete for book, author and publisher
//
// On top of the required operations the schema exposes relationships between the
// three types, so a single request can walk the graph:
//   Publisher -> authors -> books
//   Book      -> author, publisher
// and a `search` query that returns a `SearchResult` union.

export const typeDefs = /* GraphQL */ `
  "A book in the library."
  type Book {
    id: Int!
    title: String!
    publishingYear: Int!

    "The author of this book."
    author: Author

    "The publishing company of this book."
    publisher: Publisher
  }

  "An author of one or more books."
  type Author {
    id: Int!
    name: String!
    surname: String!

    "All books written by this author."
    books: [Book!]!
  }

  "A publishing company that publishes books."
  type Publisher {
    id: Int!
    name: String!

    "The distinct authors of the books published by this company."
    authors: [Author!]!

    "All books published by this company."
    books: [Book!]!
  }

  "A book, an author or a publisher returned by the search query."
  union SearchResult = Book | Author | Publisher

  type Query {
    "Get a single book by its ID. Returns NOT_FOUND when the book does not exist."
    book(id: Int!): Book

    "List all authors."
    authors: [Author!]!

    "List all publishers."
    publishers: [Publisher!]!

    "Search books, authors and publishers whose title/name contains the term. Demonstrates GraphQL unions."
    search(term: String!): [SearchResult!]!
  }

  type Mutation {
    "Create a new book. Returns the created book."
    createBook(
      title: String!
      authorId: Int!
      publishingCompanyId: Int!
      publishingYear: Int!
    ): Book!

    "Update all fields of an existing book. Returns the updated book."
    updateBook(
      id: Int!
      title: String!
      authorId: Int!
      publishingCompanyId: Int!
      publishingYear: Int!
    ): Book!

    "Delete a book by ID. Returns true when a book was deleted."
    deleteBook(id: Int!): Boolean!

    "Create a new author. Returns the created author."
    createAuthor(name: String!, surname: String!): Author!

    "Update all fields of an existing author. Returns the updated author."
    updateAuthor(id: Int!, name: String!, surname: String!): Author!

    "Delete an author by ID. Fails with CONFLICT while books reference the author."
    deleteAuthor(id: Int!): Boolean!

    "Create a new publisher. Returns the created publisher."
    createPublisher(name: String!): Publisher!

    "Update an existing publisher. Returns the updated publisher."
    updatePublisher(id: Int!, name: String!): Publisher!

    "Delete a publisher by ID. Fails with CONFLICT while books reference the publisher."
    deletePublisher(id: Int!): Boolean!
  }
`;
