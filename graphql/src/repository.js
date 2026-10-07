// Data access layer.
//
// Maps the physical SQLite column names (nBookID, cTitle, ...) to the logical
// GraphQL field names (id, title, ...) and exposes small CRUD helpers that are
// used by the resolvers.

import { db } from './db.js';

const toBook = (row) =>
  row && {
    id: row.nBookID,
    title: row.cTitle,
    authorId: row.nAuthorID,
    publishingCompanyId: row.nPublishingCompanyID,
    publishingYear: row.nPublishingYear,
  };

const toAuthor = (row) =>
  row && {
    id: row.nAuthorID,
    name: row.cName,
    surname: row.cSurname ?? '',
  };

const toPublisher = (row) =>
  row && {
    id: row.nPublishingCompanyID,
    name: row.cName,
  };

/* ------------------------------- Books ------------------------------- */

export function findBookById(id) {
  return toBook(db.prepare('SELECT * FROM tbook WHERE nBookID = ?').get(id));
}

export function insertBook({ title, authorId, publishingCompanyId, publishingYear }) {
  const result = db
    .prepare(
      'INSERT INTO tbook (cTitle, nAuthorID, nPublishingYear, nPublishingCompanyID) VALUES (?, ?, ?, ?)'
    )
    .run(title, authorId, publishingYear, publishingCompanyId);
  return findBookById(Number(result.lastInsertRowid));
}

export function updateBookById(id, { title, authorId, publishingCompanyId, publishingYear }) {
  db.prepare(
    'UPDATE tbook SET cTitle = ?, nAuthorID = ?, nPublishingYear = ?, nPublishingCompanyID = ? WHERE nBookID = ?'
  ).run(title, authorId, publishingYear, publishingCompanyId, id);
  return findBookById(id);
}

export function deleteBookById(id) {
  return db.prepare('DELETE FROM tbook WHERE nBookID = ?').run(id).changes > 0;
}

export function countBooksByAuthorId(authorId) {
  return db.prepare('SELECT COUNT(*) AS total FROM tbook WHERE nAuthorID = ?').get(authorId).total;
}

export function countBooksByPublisherId(publisherId) {
  return db
    .prepare('SELECT COUNT(*) AS total FROM tbook WHERE nPublishingCompanyID = ?')
    .get(publisherId).total;
}

export function listBooksByAuthorId(authorId) {
  return db
    .prepare('SELECT * FROM tbook WHERE nAuthorID = ? ORDER BY nBookID')
    .all(authorId)
    .map(toBook);
}

export function listBooksByPublisherId(publisherId) {
  return db
    .prepare('SELECT * FROM tbook WHERE nPublishingCompanyID = ? ORDER BY nBookID')
    .all(publisherId)
    .map(toBook);
}

/* ------------------------------ Authors ------------------------------ */

export function findAuthorById(id) {
  return toAuthor(db.prepare('SELECT * FROM tauthor WHERE nAuthorID = ?').get(id));
}

export function listAuthors() {
  return db.prepare('SELECT * FROM tauthor ORDER BY nAuthorID').all().map(toAuthor);
}

// The distinct authors of the books published by the given publishing company.
export function listAuthorsByPublisherId(publisherId) {
  return db
    .prepare(
      `SELECT DISTINCT a.*
         FROM tauthor a
         INNER JOIN tbook b ON b.nAuthorID = a.nAuthorID
        WHERE b.nPublishingCompanyID = ?
        ORDER BY a.nAuthorID`
    )
    .all(publisherId)
    .map(toAuthor);
}

export function insertAuthor({ name, surname }) {
  const result = db.prepare('INSERT INTO tauthor (cName, cSurname) VALUES (?, ?)').run(name, surname);
  return findAuthorById(Number(result.lastInsertRowid));
}

export function updateAuthorById(id, { name, surname }) {
  db.prepare('UPDATE tauthor SET cName = ?, cSurname = ? WHERE nAuthorID = ?').run(name, surname, id);
  return findAuthorById(id);
}

export function deleteAuthorById(id) {
  return db.prepare('DELETE FROM tauthor WHERE nAuthorID = ?').run(id).changes > 0;
}

/* ----------------------------- Publishers ---------------------------- */

export function findPublisherById(id) {
  return toPublisher(
    db.prepare('SELECT * FROM tpublishingcompany WHERE nPublishingCompanyID = ?').get(id)
  );
}

export function listPublishers() {
  return db.prepare('SELECT * FROM tpublishingcompany ORDER BY nPublishingCompanyID').all().map(toPublisher);
}

export function insertPublisher({ name }) {
  const result = db.prepare('INSERT INTO tpublishingcompany (cName) VALUES (?)').run(name);
  return findPublisherById(Number(result.lastInsertRowid));
}

export function updatePublisherById(id, { name }) {
  db.prepare('UPDATE tpublishingcompany SET cName = ? WHERE nPublishingCompanyID = ?').run(name, id);
  return findPublisherById(id);
}

export function deletePublisherById(id) {
  return db.prepare('DELETE FROM tpublishingcompany WHERE nPublishingCompanyID = ?').run(id).changes > 0;
}

/* ------------------------------ Search ------------------------------- */

// Case-insensitive substring search across the three tables. Each result keeps
// the shape of its own type, which is what lets the `SearchResult` union tell
// them apart in the resolver (`__resolveType`).
const SEARCH_LIMIT = 25;

export function search(term) {
  const like = `%${term}%`;

  const books = db
    .prepare('SELECT * FROM tbook WHERE cTitle LIKE ? ORDER BY nBookID LIMIT ?')
    .all(like, SEARCH_LIMIT)
    .map(toBook);

  const authors = db
    .prepare('SELECT * FROM tauthor WHERE cName LIKE ? OR cSurname LIKE ? ORDER BY nAuthorID LIMIT ?')
    .all(like, like, SEARCH_LIMIT)
    .map(toAuthor);

  const publishers = db
    .prepare('SELECT * FROM tpublishingcompany WHERE cName LIKE ? ORDER BY nPublishingCompanyID LIMIT ?')
    .all(like, SEARCH_LIMIT)
    .map(toPublisher);

  return [...books, ...authors, ...publishers];
}

