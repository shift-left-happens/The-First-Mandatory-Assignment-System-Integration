'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const Database = require('better-sqlite3');
const { library, startServer } = require('./server');
const { startReplica } = require('./replica');

test('lookup, validation, creation, and two WatchBooks subscribers', async () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'library-grpc-'));
  const dbPath = path.join(dir, 'test.db');
  const setup = new Database(dbPath);
  setup.exec(`
    CREATE TABLE tauthor (nAuthorID INTEGER PRIMARY KEY, cName TEXT NOT NULL);
    CREATE TABLE tpublishingcompany (nPublishingCompanyID INTEGER PRIMARY KEY, cName TEXT NOT NULL);
    CREATE TABLE tbook (nBookID INTEGER PRIMARY KEY AUTOINCREMENT, cTitle TEXT NOT NULL,
      nAuthorID INTEGER NOT NULL, nPublishingYear INTEGER, nPublishingCompanyID INTEGER NOT NULL);
    INSERT INTO tauthor VALUES (1, 'Ada');
    INSERT INTO tpublishingcompany VALUES (2, 'Example Press');
  `);
  setup.close();

  const { server, db, port } = await startServer({ dbPath, port: 0 });
  const client = new library.LibraryService(`127.0.0.1:${port}`, grpc.credentials.createInsecure());
  const reflectionDefinition = protoLoader.loadSync(path.join(
    __dirname, 'node_modules/@grpc/reflection/build/proto/grpc/reflection/v1/reflection.proto',
  ));
  const reflectionApi = grpc.loadPackageDefinition(reflectionDefinition).grpc.reflection.v1;
  const reflectionClient = new reflectionApi.ServerReflection(`127.0.0.1:${port}`, grpc.credentials.createInsecure());
  const rpc = (method, request) => new Promise((resolve, reject) => {
    client[method](request, (err, response) => err ? reject(err) : resolve(response));
  });
  const streams = [];
  let replica;

  try {
    const reflectedServices = await new Promise((resolve, reject) => {
      const stream = reflectionClient.serverReflectionInfo();
      stream.once('data', (response) => { resolve(response.listServicesResponse.service.map((item) => item.name)); stream.cancel(); });
      stream.once('error', reject);
      stream.write({ listServices: '' });
    });
    assert.ok(reflectedServices.includes('library.LibraryService'));
    await assert.rejects(rpc('getBookById', { id: 99 }), { code: grpc.status.NOT_FOUND });
    await assert.rejects(rpc('createBook', {
      title: 'Bad year', authorId: 1, publishingCompanyId: 2, publishingYear: 1899,
    }), { code: grpc.status.INVALID_ARGUMENT });
    await assert.rejects(rpc('createBook', {
      title: 'Bad author', authorId: 99, publishingCompanyId: 2, publishingYear: 2020,
    }), { code: grpc.status.INVALID_ARGUMENT });

    for (let i = 0; i < 2; i++) {
      const stream = client.watchBooks({});
      streams.push(stream);
      await new Promise((resolve, reject) => {
        stream.once('metadata', resolve);
        stream.once('error', reject);
      });
    }
    const received = streams.map((stream) => new Promise((resolve, reject) => {
      stream.once('data', resolve);
      stream.once('error', reject);
    }));
    let replicaSaved;
    const saved = new Promise((resolve) => { replicaSaved = resolve; });
    replica = await startReplica({
      address: `127.0.0.1:${port}`,
      dbPath: path.join(dir, 'replica.db'),
      onBook: replicaSaved,
    });
    const created = await rpc('createBook', {
      title: '  Test book  ', authorId: 1, publishingCompanyId: 2, publishingYear: 2020,
    });
    assert.ok(created.id > 0);
    const book = await rpc('getBookById', { id: created.id });
    assert.deepEqual(book, {
      id: created.id, title: 'Test book', authorId: 1,
      publishingCompanyId: 2, publishingYear: 2020,
    });
    assert.deepEqual(await Promise.all(received), [book, book]);
    assert.equal((await saved).id, created.id);
    assert.deepEqual(replica.db.prepare('SELECT * FROM books WHERE id = ?').get(created.id), {
      id: created.id, title: 'Test book', author_id: 1,
      publishing_company_id: 2, publishing_year: 2020,
    });
  } finally {
    if (replica) replica.close();
    streams.forEach((stream) => stream.cancel());
    client.close();
    reflectionClient.close();
    await new Promise((resolve) => server.tryShutdown(resolve));
    db.close();
    fs.rmSync(dir, { recursive: true, force: true });
  }
});
