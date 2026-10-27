package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.service.BookService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final ResourceLinks links;

    public BookController(BookService bookService, ResourceLinks links) {
        this.bookService = bookService;
        this.links = links;
    }

    @PostMapping
    public ResponseEntity<EntityModel<Book>> createBook(@RequestBody Book book) {
        EntityModel<Book> created = links.book(bookService.createBook(book));
        return ResponseEntity.created(created.getRequiredLink("self").toUri()).body(created);
    }

    @GetMapping("/{id}")
    public EntityModel<Book> getBookById(@PathVariable Long id) {
        return links.book(bookService.getBookById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found")));
    }

    @GetMapping
    public CollectionModel<EntityModel<Book>> getAllBooks(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        boolean paged = limit != null || offset != null;
        int actualLimit = limit != null ? limit : 10;
        int actualOffset = offset != null ? offset : 0;
        List<Book> books = paged
                ? bookService.getBooksWithPagination(actualLimit, actualOffset)
                : bookService.getAllBooks();
        CollectionModel<EntityModel<Book>> result = CollectionModel.of(
                books.stream().map(links::book).toList(),
                linkTo(methodOn(BookController.class).getAllBooks(limit, offset)).withSelfRel());
        if (paged) {
            if ((long) actualOffset + books.size() < bookService.countBooks()) {
                result.add(linkTo(methodOn(BookController.class)
                        .getAllBooks(actualLimit, actualOffset + actualLimit)).withRel("next"));
            }
            if (actualOffset > 0) {
                result.add(linkTo(methodOn(BookController.class)
                        .getAllBooks(actualLimit, Math.max(0, actualOffset - actualLimit))).withRel("prev"));
            }
        }
        return result;
    }

    @PutMapping("/{id}")
    public EntityModel<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        return links.book(bookService.updateBook(id, book));
    }

    @PatchMapping("/{id}")
    public EntityModel<Book> patchBook(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return links.book(bookService.patchBook(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
