// Smoke test for the GraphQL API.
//
// Starts the server on a temporary copy of the database, runs every query and
// mutation (including the error cases) and checks the expected results.
// The real database/library.db is never modified.
//
// Run with: npm run smoke

import { spawn } from 'node:child_process';
import { copyFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';

const sourceDb = fileURLToPath(new URL('../../database/library.db', import.meta.url));
const testDb = join(tmpdir(), `library-graphql-smoke-${Date.now()}.db`);
const entry = fileURLToPath(new URL('../src/index.js', import.meta.url));

const PORT = 4123;
const url = `http://localhost:${PORT}/graphql`;

copyFileSync(sourceDb, testDb);

const server = spawn(process.execPath, [entry], {
  env: { ...process.env, PORT: String(PORT), HOST: '127.0.0.1', DB_PATH: testDb },
  stdio: ['ignore', 'pipe', 'pipe'],
});

let passed = 0;
let failed = 0;

function check(name, condition) {
  if (condition) {
    passed++;
    console.log(`  PASS  ${name}`);
  } else {
    failed++;
    console.log(`  FAIL  ${name}`);
  }
}

async function graphql(query, variables) {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ query, variables }),
  });
  return response.json();
}

async function waitForServer() {
  for (let attempt = 0; attempt < 60; attempt++) {
    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ query: '{ __typename }' }),
      });
      if (response.ok) return;
    } catch {
      // server not ready yet
    }
    await new Promise((resolve) => setTimeout(resolve, 200));
  }
  throw new Error('Server did not start in time.');
}

async function main() {
  await waitForServer();
  console.log('\nGraphQL API smoke test\n');

  // --- Create ---
  const author = (
    await graphql(`mutation { createAuthor(name: "Test", surname: "Author") { id name surname } }`)
  ).data.createAuthor;
  check('createAuthor returns the generated id', Number.isInteger(author.id) && author.name === 'Test');

  const publisher = (
    await graphql(`mutation { createPublisher(name: "Test Publisher") { id name } }`)
  ).data.createPublisher;
  check('createPublisher returns the generated id', Number.isInteger(publisher.id));

  const book = (
    await graphql(
      `mutation ($a: Int!, $p: Int!) {
         createBook(title: "Smoke Test Book", authorId: $a, publishingCompanyId: $p, publishingYear: 2024) {
           id title publishingYear author { id } publisher { id }
         }
       }`,
      { a: author.id, p: publisher.id }
    )
  ).data.createBook;
  check(
    'createBook returns the new book',
    book.title === 'Smoke Test Book' && book.author?.id === author.id && book.publisher?.id === publisher.id
  );

  // --- Read ---
  const fetched = (await graphql(`query ($id: Int!) { book(id: $id) { id title } }`, { id: book.id })).data.book;
  check('book query returns the created book', fetched && fetched.id === book.id);

  const authors = (await graphql(`{ authors { id name surname } }`)).data.authors;
  check('authors query returns a list', Array.isArray(authors) && authors.length > 0);

  const publishers = (await graphql(`{ publishers { id name } }`)).data.publishers;
  check('publishers query returns a list', Array.isArray(publishers) && publishers.length > 0);

  // --- Nesting: Book -> author / publisher ---
  const bookWithRelations = (
    await graphql(`query ($id: Int!) { book(id: $id) { id author { id name surname } publisher { id name } } }`, {
      id: book.id,
    })
  ).data.book;
  check(
    'book resolves its author and publisher',
    bookWithRelations.author?.id === author.id && bookWithRelations.publisher?.id === publisher.id
  );

  // --- Nesting: Author -> books ---
  const authorBooks = (
    await graphql(`{ authors { id books { id title } } }`)
  ).data.authors.find((a) => a.id === author.id).books;
  check('author nests its books', authorBooks.some((b) => b.id === book.id));

  // --- Nesting: Publisher -> authors -> books (a single request walks the graph) ---
  const nestedPublisher = (
    await graphql(
      `{
         publishers {
           id
           name
           authors { id name surname books { id title } }
         }
       }`
    )
  ).data.publishers.find((p) => p.id === publisher.id);
  check(
    'publisher nests authors which nest books',
    nestedPublisher?.authors.some((a) => a.id === author.id && a.books.some((b) => b.id === book.id))
  );

  // --- Unions: search across the three types ---
  const searchHits = (
    await graphql(
      `query ($t: String!) {
         search(term: $t) {
           __typename
           ... on Book { id title }
           ... on Author { id name surname }
           ... on Publisher { id name }
         }
       }`,
      { t: 'Smoke' }
    )
  ).data.search;
  check(
    'search returns a union with __typename per hit',
    Array.isArray(searchHits) && searchHits.some((hit) => hit.__typename === 'Book' && hit.id === book.id)
  );

  // --- Update ---
  const updatedBook = (
    await graphql(
      `mutation ($id: Int!, $a: Int!, $p: Int!) {
         updateBook(id: $id, title: "Updated Title", authorId: $a, publishingCompanyId: $p, publishingYear: 2025) {
           id title publishingYear
         }
       }`,
      { id: book.id, a: author.id, p: publisher.id }
    )
  ).data.updateBook;
  check('updateBook updates all fields', updatedBook.title === 'Updated Title' && updatedBook.publishingYear === 2025);

  const updatedAuthor = (
    await graphql(
      `mutation ($id: Int!) { updateAuthor(id: $id, name: "Updated", surname: "Name") { id name surname } }`,
      { id: author.id }
    )
  ).data.updateAuthor;
  check('updateAuthor updates all fields', updatedAuthor.name === 'Updated' && updatedAuthor.surname === 'Name');

  const updatedPublisher = (
    await graphql(
      `mutation ($id: Int!) { updatePublisher(id: $id, name: "Updated Publisher") { id name } }`,
      { id: publisher.id }
    )
  ).data.updatePublisher;
  check('updatePublisher updates all fields', updatedPublisher.name === 'Updated Publisher');

  // --- Validation errors ---
  const badYear = await graphql(
    `mutation ($a: Int!, $p: Int!) {
       createBook(title: "Bad", authorId: $a, publishingCompanyId: $p, publishingYear: 1800) { id }
     }`,
    { a: author.id, p: publisher.id }
  );
  check(
    'createBook rejects a publishing year below 1900 (VALIDATION_ERROR)',
    badYear.errors?.[0]?.extensions?.code === 'VALIDATION_ERROR'
  );

  const badAuthor = await graphql(
    `mutation ($p: Int!) {
       createBook(title: "Bad", authorId: 999999, publishingCompanyId: $p, publishingYear: 2024) { id }
     }`,
    { p: publisher.id }
  );
  check(
    'createBook rejects a non-existing author (VALIDATION_ERROR)',
    badAuthor.errors?.[0]?.extensions?.code === 'VALIDATION_ERROR'
  );

  const badPublisher = await graphql(
    `mutation ($a: Int!) {
       createBook(title: "Bad", authorId: $a, publishingCompanyId: 999999, publishingYear: 2024) { id }
     }`,
    { a: author.id }
  );
  check(
    'createBook rejects a non-existing publisher (VALIDATION_ERROR)',
    badPublisher.errors?.[0]?.extensions?.code === 'VALIDATION_ERROR'
  );

  // --- Not found ---
  const missingBook = await graphql(`query { book(id: 999999) { id } }`);
  check('book query returns NOT_FOUND for a missing book', missingBook.errors?.[0]?.extensions?.code === 'NOT_FOUND');

  // --- Delete conflict (author) ---
  const conflictAuthor = await graphql(`mutation ($id: Int!) { deleteAuthor(id: $id) }`, { id: author.id });
  check(
    'deleteAuthor returns CONFLICT while a book references it',
    conflictAuthor.errors?.[0]?.extensions?.code === 'CONFLICT'
  );

  // --- Delete (happy path) ---
  const deletedBook = (await graphql(`mutation ($id: Int!) { deleteBook(id: $id) }`, { id: book.id })).data.deleteBook;
  check('deleteBook returns true', deletedBook === true);

  const deletedAuthor = (await graphql(`mutation ($id: Int!) { deleteAuthor(id: $id) }`, { id: author.id })).data
    .deleteAuthor;
  check('deleteAuthor returns true once nothing references it', deletedAuthor === true);

  const deletedPublisher = (await graphql(`mutation ($id: Int!) { deletePublisher(id: $id) }`, { id: publisher.id }))
    .data.deletePublisher;
  check('deletePublisher returns true', deletedPublisher === true);

  // --- Publisher delete conflict ---
  const author2 = (await graphql(`mutation { createAuthor(name: "Author Two", surname: "X") { id } }`)).data
    .createAuthor;
  const publisher2 = (await graphql(`mutation { createPublisher(name: "Publisher Two") { id } }`)).data.createPublisher;
  await graphql(
    `mutation ($a: Int!, $p: Int!) { createBook(title: "Book Two", authorId: $a, publishingCompanyId: $p, publishingYear: 2000) { id } }`,
    { a: author2.id, p: publisher2.id }
  );
  const conflictPublisher = await graphql(`mutation ($id: Int!) { deletePublisher(id: $id) }`, { id: publisher2.id });
  check(
    'deletePublisher returns CONFLICT while a book references it',
    conflictPublisher.errors?.[0]?.extensions?.code === 'CONFLICT'
  );

  console.log(`\n${passed} passed, ${failed} failed\n`);
  return failed === 0;
}

function cleanup() {
  server.kill();
  for (const file of [testDb, `${testDb}-wal`, `${testDb}-shm`]) {
    try {
      rmSync(file, { force: true });
    } catch {
      // ignore files that are still locked by the OS
    }
  }
}

main()
  .then((ok) => {
    cleanup();
    process.exit(ok ? 0 : 1);
  })
  .catch((error) => {
    console.error(error);
    cleanup();
    process.exit(1);
  });
