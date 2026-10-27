package com.example.demo.service;

import com.example.demo.model.Book;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;

    public BookService(
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            PublisherRepository publisherRepository) {

        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
    }

    public Book createBook(Book book) {

        validateBook(book);

        return bookRepository.save(book);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getBooksWithPagination(int limit, int offset) {

        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative");
        }

        return bookRepository.findBooksWithPagination(limit, offset);
    }

    public Book updateBook(Long id, Book updatedBook) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        validateBook(updatedBook);

        book.setTitle(updatedBook.getTitle());
        book.setAuthorId(updatedBook.getAuthorId());
        book.setPublishingCompanyId(updatedBook.getPublishingCompanyId());
        book.setPublishingYear(updatedBook.getPublishingYear());

        return bookRepository.save(book);
    }

    public Book patchBook(Long id, Map<String, Object> updates) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (updates.containsKey("title")) {
            book.setTitle((String) updates.get("title"));
        }

        if (updates.containsKey("authorId")) {
            book.setAuthorId(((Number) updates.get("authorId")).longValue());
        }

        if (updates.containsKey("publishingCompanyId")) {
            book.setPublishingCompanyId(
                    ((Number) updates.get("publishingCompanyId")).longValue());
        }

        if (updates.containsKey("publishingYear")) {
            book.setPublishingYear(
                    ((Number) updates.get("publishingYear")).intValue());
        }

        validateBook(book);

        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    private void validateBook(Book book) {

        if (!authorRepository.existsById(book.getAuthorId())) {
            throw new IllegalArgumentException("Author does not exist");
        }

        if (!publisherRepository.existsById(book.getPublishingCompanyId())) {
            throw new IllegalArgumentException("Publisher does not exist");
        }

        if (book.getPublishingYear() < 1900) {
            throw new IllegalArgumentException(
                    "Publishing year must be greater than or equal to 1900");
        }
    }
}