// GraphQL API entry point.
//
// Exposes a single POST HTTP endpoint at /graphql using GraphQL Yoga.
// A browser visiting /graphql is served the GraphiQL IDE for interactive use.

import { createServer } from 'node:http';
import { createSchema, createYoga } from 'graphql-yoga';

import { typeDefs } from './schema.js';
import { resolvers } from './resolvers.js';
import { db, dbPath, ensureSchema } from './db.js';

const PORT = Number(process.env.PORT ?? 4000);
const HOST = process.env.HOST ?? '0.0.0.0';

// Make sure the tables exist before serving requests.
ensureSchema();

const schema = createSchema({ typeDefs, resolvers });

const yoga = createYoga({
  schema,
  graphqlEndpoint: '/graphql',
  graphiql: true,
});

const server = createServer(yoga);

server.listen(PORT, HOST, () => {
  console.log('----------------------------------------------------');
  console.log(' Library GraphQL API');
  console.log(` Endpoint : http://localhost:${PORT}/graphql`);
  console.log(` Database : ${dbPath}`);
  console.log('----------------------------------------------------');
});

// Close the HTTP server and the database cleanly on shutdown.
function shutdown() {
  server.close(() => {
    db.close();
    process.exit(0);
  });
}

process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
