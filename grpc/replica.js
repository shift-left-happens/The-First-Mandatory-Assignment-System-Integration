'use strict';

const path = require('node:path');
const grpc = require('@grpc/grpc-js');
const Database = require('better-sqlite3');
const { GrpcReflection } = require('grpc-js-reflection-client');

async function startReplica({
  address = '127.0.0.1:50051',
  dbPath = path.join(__dirname, 'replica.db'),
  onBook = (book) => console.log(`Saved book ${book.id}: ${book.title}`),
} = {}) {
  const db = new Database(dbPath);
  db.exec(`
    CREATE TABLE IF NOT EXISTS books (
      id INTEGER PRIMARY KEY,
      title TEXT NOT NULL,
      author_id INTEGER NOT NULL,
      publishing_company_id INTEGER NOT NULL,
      publishing_year INTEGER NOT NULL
    )
  `);
  const saveBook = db.prepare(`
    INSERT INTO books (id, title, author_id, publishing_company_id, publishing_year)
    VALUES (@id, @title, @authorId, @publishingCompanyId, @publishingYear)
    ON CONFLICT(id) DO UPDATE SET
      title = excluded.title,
      author_id = excluded.author_id,
      publishing_company_id = excluded.publishing_company_id,
      publishing_year = excluded.publishing_year
  `);
  const credentials = grpc.credentials.createInsecure();
  let client;
  try {
    const reflection = new GrpcReflection(address, credentials);
    const descriptor = await reflection.getDescriptorBySymbol('library.LibraryService');
    const discovered = descriptor.getPackageObject({ keepCase: false, defaults: true });
    client = new discovered.library.LibraryService(address, credentials);
  } catch (err) {
    db.close();
    throw err;
  }
  const stream = client.watchBooks({});

  stream.on('data', (book) => {
    try {
      saveBook.run(book);
      onBook(book);
    } catch (err) {
      console.error('Could not save received book:', err);
    }
  });
  stream.on('error', (err) => {
    if (err.code !== grpc.status.CANCELLED) {
      console.error('WatchBooks connection ended:', err.message);
    }
  });

  function close() {
    stream.cancel();
    client.close();
    db.close();
  }

  try {
    await new Promise((resolve, reject) => {
      stream.once('metadata', resolve);
      stream.once('error', reject);
    });
  } catch (err) {
    close();
    throw err;
  }
  return { db, close };
}

if (require.main === module) {
  startReplica({
    address: process.env.GRPC_ADDRESS || '127.0.0.1:50051',
    dbPath: process.env.REPLICA_DB || path.join(__dirname, 'replica.db'),
  })
    .then((replica) => {
      console.log('Discovered LibraryService through reflection. Listening for new books. Press Ctrl+C to stop.');
      process.on('SIGINT', () => { replica.close(); process.exit(0); });
    })
    .catch((err) => { console.error('Could not start replica:', err); process.exitCode = 1; });
}

module.exports = { startReplica };
