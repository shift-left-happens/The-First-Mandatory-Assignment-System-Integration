
package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.service.BookService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
    }

    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {

        if (limit != null || offset != null) {
            int actualLimit = limit != null ? limit : 10;
            int actualOffset = offset != null ? offset : 0;

            return bookService.getBooksWithPagination(actualLimit, actualOffset);
        }

        return bookService.getAllBooks();
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
