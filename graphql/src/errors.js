// Error helpers.
//
// Every expected failure is raised as a GraphQLError with a machine-readable
// `extensions.code`, mirroring the fault types used by the SOAP API:
//
//   VALIDATION_ERROR -> ValidationFault (missing/invalid values, bad references)
//   NOT_FOUND        -> NotFoundFault   (requested ID does not exist)
//   CONFLICT         -> ConflictFault   (delete of an entity that is referenced)

import { GraphQLError } from 'graphql';

const makeError = (code, message) => new GraphQLError(message, { extensions: { code } });

export const notFound = (message) => makeError('NOT_FOUND', message);
export const validationError = (message) => makeError('VALIDATION_ERROR', message);
export const conflict = (message) => makeError('CONFLICT', message);
