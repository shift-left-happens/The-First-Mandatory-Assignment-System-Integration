package com.example.demo.service;

import com.example.demo.model.Book;
import com.example.demo.request.BookPatchRequest;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

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

    public long countBooks() {
        return bookRepository.count();
    }

    public List<Book> getBooksByAuthor(Long authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new NoSuchElementException("Author not found");
        }
        return bookRepository.findByAuthorId(authorId);
    }

    public List<Book> getBooksByPublisher(Long publisherId) {
        if (!publisherRepository.existsById(publisherId)) {
            throw new NoSuchElementException("Publisher not found");
        }
        return bookRepository.findByPublishingCompanyId(publisherId);
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
                .orElseThrow(() -> new NoSuchElementException("Book not found"));

        validateBook(updatedBook);

        book.setTitle(updatedBook.getTitle());
        book.setAuthorId(updatedBook.getAuthorId());
        book.setPublishingCompanyId(updatedBook.getPublishingCompanyId());
        book.setPublishingYear(updatedBook.getPublishingYear());

        return bookRepository.save(book);
    }

    public Book patchBook(Long id, BookPatchRequest updates) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));

        Book candidate = new Book(book.getTitle(), book.getAuthorId(),
                book.getPublishingCompanyId(), book.getPublishingYear());
        if (updates.body().has("title")) candidate.setTitle(updates.body().get("title").textValue());
        if (updates.body().has("authorId")) candidate.setAuthorId(updates.body().get("authorId").longValue());
        if (updates.body().has("publishingCompanyId")) {
            candidate.setPublishingCompanyId(updates.body().get("publishingCompanyId").longValue());
        }
        if (updates.body().has("publishingYear")) {
            candidate.setPublishingYear(updates.body().get("publishingYear").intValue());
        }

        validateBook(candidate);
        book.setTitle(candidate.getTitle());
        book.setAuthorId(candidate.getAuthorId());
        book.setPublishingCompanyId(candidate.getPublishingCompanyId());
        book.setPublishingYear(candidate.getPublishingYear());

        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NoSuchElementException("Book not found");
        }
        bookRepository.deleteById(id);
    }

    private void validateBook(Book book) {

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (book.getAuthorId() == null || !authorRepository.existsById(book.getAuthorId())) {
            throw new IllegalArgumentException("Author does not exist");
        }

        if (book.getPublishingCompanyId() == null || !publisherRepository.existsById(book.getPublishingCompanyId())) {
            throw new IllegalArgumentException("Publisher does not exist");
        }

        if (book.getPublishingYear() == null || book.getPublishingYear() < 1900) {
            throw new IllegalArgumentException(
                    "Publishing year must be greater than or equal to 1900");
        }
    }
}
