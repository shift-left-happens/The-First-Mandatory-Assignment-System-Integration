
package com.example.demo.controller;

import com.example.demo.model.Author;
import com.example.demo.service.AuthorService;
import com.example.demo.service.BookService;
import com.example.demo.model.Book;
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
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorService authorService;
    private final ResourceLinks links;
    private final BookService bookService;

    public AuthorController(AuthorService authorService, ResourceLinks links, BookService bookService) {
        this.authorService = authorService;
        this.links = links;
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<EntityModel<Author>> createAuthor(@RequestBody Author author) {
        EntityModel<Author> created = links.author(authorService.createAuthor(author));
        return ResponseEntity.created(created.getRequiredLink("self").toUri()).body(created);
    }

    @GetMapping("/{id}")
    public EntityModel<Author> getAuthorById(@PathVariable Long id) {
        return links.author(authorService.getAuthorById(id)
                .orElseThrow(() -> new NoSuchElementException("Author not found")));
    }

    @GetMapping
    public CollectionModel<EntityModel<Author>> getAllAuthors() {
        return CollectionModel.of(authorService.getAllAuthors().stream().map(links::author).toList(),
                linkTo(methodOn(AuthorController.class).getAllAuthors()).withSelfRel());
    }

    @GetMapping("/{id}/books")
    public CollectionModel<EntityModel<Book>> getBooksByAuthor(@PathVariable Long id) {
        return CollectionModel.of(bookService.getBooksByAuthor(id).stream().map(links::book).toList(),
                linkTo(methodOn(AuthorController.class).getBooksByAuthor(id)).withSelfRel(),
                linkTo(methodOn(AuthorController.class).getAuthorById(id)).withRel("author"));
    }

    @PutMapping("/{id}")
    public EntityModel<Author> updateAuthor(
            @PathVariable Long id,
            @RequestBody Author author) {
        return links.author(authorService.updateAuthor(id, author));
    }

    @PatchMapping("/{id}")
    public EntityModel<Author> patchAuthor(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return links.author(authorService.patchAuthor(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
