'use strict';

const path = require('node:path');
const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const { ReflectionService } = require('@grpc/reflection');
const Database = require('better-sqlite3');

const definition = protoLoader.loadSync(path.join(__dirname, 'library.proto'), {
  keepCase: false,
  defaults: true,
});
const library = grpc.loadPackageDefinition(definition).library;

function makeService(db) {
  const subscribers = new Set();
  const getBook = db.prepare(`
    SELECT nBookID AS id, cTitle AS title, nAuthorID AS authorId,
           nPublishingCompanyID AS publishingCompanyId,
           nPublishingYear AS publishingYear
    FROM tbook WHERE nBookID = ?
  `);
  const authorExists = db.prepare('SELECT 1 FROM tauthor WHERE nAuthorID = ?');
  const publisherExists = db.prepare('SELECT 1 FROM tpublishingcompany WHERE nPublishingCompanyID = ?');
  const insertBook = db.prepare(`
    INSERT INTO tbook (cTitle, nAuthorID, nPublishingCompanyID, nPublishingYear)
    VALUES (?, ?, ?, ?)
  `);

  function error(code, message) {
    return Object.assign(new Error(message), { code });
  }

  function getBookById(call, callback) {
    const id = call.request.id;
    if (!Number.isInteger(id) || id <= 0) {
      return callback(error(grpc.status.INVALID_ARGUMENT, 'id must be a positive integer'));
    }
    try {
      const book = getBook.get(id);
      if (!book) return callback(error(grpc.status.NOT_FOUND, 'Book not found'));
      callback(null, book);
    } catch (err) {
      callback(error(grpc.status.INTERNAL, 'Could not read book'));
    }
  }

  function createBook(call, callback) {
    const { title, authorId, publishingCompanyId, publishingYear } = call.request;
    if (typeof title !== 'string' || !title.trim() || title.length > 255) {
      return callback(error(grpc.status.INVALID_ARGUMENT, 'title must contain 1–255 characters'));
    }
    if (!Number.isInteger(authorId) || authorId <= 0) {
      return callback(error(grpc.status.INVALID_ARGUMENT,
        `authorId must be a positive integer (received ${JSON.stringify(authorId)}; request fields: ${Object.keys(call.request).join(', ')})`));
    }
    if (!Number.isInteger(publishingCompanyId) || publishingCompanyId <= 0) {
      return callback(error(grpc.status.INVALID_ARGUMENT, 'publishingCompanyId must be a positive integer'));
    }
    if (!Number.isInteger(publishingYear) || publishingYear < 1900) {
      return callback(error(grpc.status.INVALID_ARGUMENT, 'publishingYear must be at least 1900'));
    }

    try {
      if (!authorExists.get(authorId)) {
        return callback(error(grpc.status.INVALID_ARGUMENT, 'Author does not exist'));
      }
      if (!publisherExists.get(publishingCompanyId)) {
        return callback(error(grpc.status.INVALID_ARGUMENT, 'Publishing company does not exist'));
      }
      const result = insertBook.run(title.trim(), authorId, publishingCompanyId, publishingYear);
      const id = Number(result.lastInsertRowid);
      const book = getBook.get(id);
      callback(null, { id });
      for (const subscriber of subscribers) {
        try {
          subscriber.write(book);
        } catch {
          subscribers.delete(subscriber);
        }
      }
    } catch (err) {
      callback(error(grpc.status.INTERNAL, 'Could not create book'));
    }
  }

  function watchBooks(call) {
    subscribers.add(call);
    const remove = () => subscribers.delete(call);
    call.on('cancelled', remove);
    call.on('error', remove);
    call.on('close', remove);
    call.sendMetadata(new grpc.Metadata());
  }

  return { getBookById, createBook, watchBooks };
}

function startServer({ dbPath = path.join(__dirname, 'library.db'), host = '127.0.0.1', port = 50051 } = {}) {
  const db = new Database(dbPath, { fileMustExist: true });
  const server = new grpc.Server();
  server.addService(library.LibraryService.service, makeService(db));
  new ReflectionService(definition).addToServer(server);
  return new Promise((resolve, reject) => {
    server.bindAsync(`${host}:${port}`, grpc.ServerCredentials.createInsecure(), (err, boundPort) => {
      if (err) {
        db.close();
        return reject(err);
      }
      resolve({ server, db, port: boundPort });
    });
  });
}

if (require.main === module) {
  const port = Number(process.env.GRPC_PORT || 50051);
  startServer({ dbPath: process.env.LIBRARY_DB || path.join(__dirname, 'library.db'), port })
    .then(({ port: boundPort }) => console.log(`Library gRPC server listening on 127.0.0.1:${boundPort}`))
    .catch((err) => { console.error(err); process.exitCode = 1; });
}

module.exports = { library, startServer };
