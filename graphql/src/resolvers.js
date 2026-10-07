// Resolvers.
//
// Each resolver validates its input, enforces the domain rules (existing
// author/publisher, publishing year >= 1900, no delete of referenced entities)
// and then delegates the actual SQL to the repository.

import * as repository from './repository.js';
import { notFound, validationError, conflict } from './errors.js';

const MIN_PUBLISHING_YEAR = 1900;

function assertRequiredString(value, field) {
  if (typeof value !== 'string' || value.trim() === '') {
    throw validationError(`Field "${field}" is required and must be a non-empty string.`);
  }
}

function assertPublishingYear(year) {
  if (!Number.isInteger(year) || year < MIN_PUBLISHING_YEAR) {
    throw validationError(
      `Field "publishingYear" must be an integer greater than or equal to ${MIN_PUBLISHING_YEAR}.`
    );
  }
}

function assertAuthorExists(authorId) {
  if (!repository.findAuthorById(authorId)) {
    throw validationError(`Author with id ${authorId} does not exist.`);
  }
}

function assertPublisherExists(publisherId) {
  if (!repository.findPublisherById(publisherId)) {
    throw validationError(`Publishing company with id ${publisherId} does not exist.`);
  }
}

export const resolvers = {
  Query: {
    book: (_parent, { id }) => {
      const book = repository.findBookById(id);
      if (!book) {
        throw notFound(`Book with id ${id} was not found.`);
      }
      return book;
    },

    authors: () => repository.listAuthors(),

    publishers: () => repository.listPublishers(),
  },

  Mutation: {
    /* ------------------------------ Books ------------------------------ */

    createBook: (_parent, { title, authorId, publishingCompanyId, publishingYear }) => {
      assertRequiredString(title, 'title');
      assertPublishingYear(publishingYear);
      assertAuthorExists(authorId);
      assertPublisherExists(publishingCompanyId);
      return repository.insertBook({ title, authorId, publishingCompanyId, publishingYear });
    },

    updateBook: (_parent, { id, title, authorId, publishingCompanyId, publishingYear }) => {
      if (!repository.findBookById(id)) {
        throw notFound(`Book with id ${id} was not found.`);
      }
      assertRequiredString(title, 'title');
      assertPublishingYear(publishingYear);
      assertAuthorExists(authorId);
      assertPublisherExists(publishingCompanyId);
      return repository.updateBookById(id, { title, authorId, publishingCompanyId, publishingYear });
    },

    deleteBook: (_parent, { id }) => {
      if (!repository.findBookById(id)) {
        throw notFound(`Book with id ${id} was not found.`);
      }
      return repository.deleteBookById(id);
    },

    /* ----------------------------- Authors ----------------------------- */

    createAuthor: (_parent, { name, surname }) => {
      assertRequiredString(name, 'name');
      return repository.insertAuthor({ name, surname });
    },

    updateAuthor: (_parent, { id, name, surname }) => {
      if (!repository.findAuthorById(id)) {
        throw notFound(`Author with id ${id} was not found.`);
      }
      assertRequiredString(name, 'name');
      return repository.updateAuthorById(id, { name, surname });
    },

    deleteAuthor: (_parent, { id }) => {
      if (!repository.findAuthorById(id)) {
        throw notFound(`Author with id ${id} was not found.`);
      }
      if (repository.countBooksByAuthorId(id) > 0) {
        throw conflict(`Author with id ${id} cannot be deleted because books reference this author.`);
      }
      return repository.deleteAuthorById(id);
    },

    /* ---------------------------- Publishers --------------------------- */

    createPublisher: (_parent, { name }) => {
      assertRequiredString(name, 'name');
      return repository.insertPublisher({ name });
    },

    updatePublisher: (_parent, { id, name }) => {
      if (!repository.findPublisherById(id)) {
        throw notFound(`Publisher with id ${id} was not found.`);
      }
      assertRequiredString(name, 'name');
      return repository.updatePublisherById(id, { name });
    },

    deletePublisher: (_parent, { id }) => {
      if (!repository.findPublisherById(id)) {
        throw notFound(`Publisher with id ${id} was not found.`);
      }
      if (repository.countBooksByPublisherId(id) > 0) {
        throw conflict(`Publisher with id ${id} cannot be deleted because books reference this publisher.`);
      }
      return repository.deletePublisherById(id);
    },
  },
};
