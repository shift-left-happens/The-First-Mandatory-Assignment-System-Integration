
package com.example.demo.service;

import com.example.demo.model.Author;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
                .orElseThrow(() -> new RuntimeException("Author not found"));

        author.setName(updatedAuthor.getName());
        author.setSurname(updatedAuthor.getSurname());

        return authorRepository.save(author);
    }

    public Author patchAuthor(Long id, Map<String, Object> updates) {

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        if (updates.containsKey("name")) {
            author.setName((String) updates.get("name"));
        }

        if (updates.containsKey("surname")) {
            author.setSurname((String) updates.get("surname"));
        }

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
}
