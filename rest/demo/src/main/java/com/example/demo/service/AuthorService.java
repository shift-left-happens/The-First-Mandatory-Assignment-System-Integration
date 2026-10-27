
package com.example.demo.service;

import com.example.demo.model.Author;
import com.example.demo.request.AuthorPatchRequest;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public Author createAuthor(Author author) {
        validateAuthor(author);
        return authorRepository.save(author);
    }

    public Optional<Author> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Author updateAuthor(Long id, Author updatedAuthor) {

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Author not found"));

        validateAuthor(updatedAuthor);

        author.setName(updatedAuthor.getName());
        author.setSurname(updatedAuthor.getSurname());

        return authorRepository.save(author);
    }

    public Author patchAuthor(Long id, AuthorPatchRequest updates) {

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Author not found"));

        Author candidate = new Author(author.getName(), author.getSurname());
        if (updates.body().has("name")) candidate.setName(updates.body().get("name").textValue());
        if (updates.body().has("surname")) candidate.setSurname(updates.body().get("surname").textValue());
        validateAuthor(candidate);
        author.setName(candidate.getName());
        author.setSurname(candidate.getSurname());
        return authorRepository.save(author);
    }

    public void deleteAuthor(Long id) {

        if (!authorRepository.existsById(id)) {
            throw new NoSuchElementException("Author not found");
        }

        if (bookRepository.existsByAuthorId(id)) {
            throw new IllegalStateException(
                    "Cannot delete author because it is referenced by existing books");
        }

        authorRepository.deleteById(id);
    }

    private void validateAuthor(Author author) {
        if (author == null || author.getName() == null || author.getName().isBlank()
                || author.getSurname() == null || author.getSurname().isBlank()) {
            throw new IllegalArgumentException("Author name and surname are required");
        }
    }
}
