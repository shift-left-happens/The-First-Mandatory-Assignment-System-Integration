// Database connection for the GraphQL API.
//
// The library data lives in a single SQLite file that is shared by all four
// APIs of the project. We use Node's built-in `node:sqlite` module so the
// project has no native dependencies, which keeps the Docker image simple and
// portable.

import { DatabaseSync } from 'node:sqlite';
import { fileURLToPath } from 'node:url';

// Default location: <repo-root>/database/library.db
// (this file lives in <repo-root>/graphql/src/, so `../../` points at the repo root).
// It can be overridden with the DB_PATH environment variable (used by Docker).
export const dbPath =
  process.env.DB_PATH ??
  fileURLToPath(new URL('../../database/library.db', import.meta.url));

// `DatabaseSync` opens the file (and creates it when missing).
export const db = new DatabaseSync(dbPath);

// Wait (instead of failing immediately) if another process is writing to the
// shared SQLite file at the same time.
db.exec('PRAGMA busy_timeout = 5000;');

// Create the tables when the database file is empty or missing. The table and
// column names mirror the schema of the provided library.db.
export function ensureSchema() {
  db.exec(`
    CREATE TABLE IF NOT EXISTS tauthor (
      nAuthorID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      cName VARCHAR(40) NOT NULL,
      cSurname VARCHAR(60) DEFAULT NULL
    );

    CREATE TABLE IF NOT EXISTS tpublishingcompany (
      nPublishingCompanyID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      cName VARCHAR(40) NOT NULL
    );

    CREATE TABLE IF NOT EXISTS tbook (
      nBookID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      cTitle VARCHAR(255) NOT NULL,
      nAuthorID INTEGER NOT NULL,
      nPublishingYear DECIMAL(4,0) DEFAULT NULL,
      nPublishingCompanyID INTEGER NOT NULL
    );

    CREATE INDEX IF NOT EXISTS idx_tbook_IDX_BOOK_AUTHOR_ID ON tbook (nAuthorID);
    CREATE INDEX IF NOT EXISTS idx_tbook_IDX_BOOK_PUBLISHING_COMPANY_ID ON tbook (nPublishingCompanyID);
  `);
}
