'use strict';

const path = require('node:path');
const Database = require('better-sqlite3');

const dbPath = process.env.REPLICA_DB || path.join(__dirname, 'replica.db');
const db = new Database(dbPath, { readonly: true, fileMustExist: true });
console.table(db.prepare('SELECT * FROM books ORDER BY id DESC LIMIT 20').all());
db.close();
