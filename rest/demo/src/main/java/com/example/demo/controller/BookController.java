
package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.service.BookService;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping("/{id}")
    public EntityModel<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));

        return toBookModel(book);
    }

    private EntityModel<Book> toBookModel(Book book) {
        return EntityModel.of(
                book,
                linkTo(methodOn(BookController.class)
                        .getBookById(book.getId())).withSelfRel(),

                linkTo(methodOn(AuthorController.class)
                        .getAuthorById(book.getAuthorId())).withRel("author"),

                linkTo(methodOn(PublisherController.class)
                        .getPublisherById(book.getPublishingCompanyId())).withRel("publisher")
        );
    }

    @GetMapping
    public CollectionModel<EntityModel<Book>> getAllBooks(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {

        List<Book> books;

        if (limit != null || offset != null) {
            int actualLimit = limit != null ? limit : 10;
            int actualOffset = offset != null ? offset : 0;

            books = bookService.getBooksWithPagination(actualLimit, actualOffset);
        } else {
            books = bookService.getAllBooks();
        }

        List<EntityModel<Book>> bookModels = books.stream()
                .map(this::toBookModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Book>> collection =
                CollectionModel.of(
                        bookModels,
                        linkTo(methodOn(BookController.class)
                                .getAllBooks(limit, offset)).withSelfRel()
                );

        if (limit != null || offset != null) {
            int actualLimit = limit != null ? limit : 10;
            int actualOffset = offset != null ? offset : 0;

            if (books.size() == actualLimit) {
                collection.add(
                        linkTo(methodOn(BookController.class)
                                .getAllBooks(actualLimit, actualOffset + actualLimit))
                                .withRel("next")
                );
            }

            if (actualOffset > 0) {
                collection.add(
                        linkTo(methodOn(BookController.class)
                                .getAllBooks(actualLimit, Math.max(0, actualOffset - actualLimit)))
                                .withRel("prev")
                );
            }
        }

        return collection;
    }

    @PutMapping("/{id}")
    public Book updateBook(
            @PathVariable Long id,
            @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    @PatchMapping("/{id}")
    public Book patchBook(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return bookService.patchBook(id, updates);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
