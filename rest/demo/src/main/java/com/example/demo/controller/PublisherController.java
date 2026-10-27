
package com.example.demo.controller;

import com.example.demo.model.Publisher;
import com.example.demo.service.PublisherService;
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
@RequestMapping("/publishers")
public class PublisherController {

    private final PublisherService publisherService;
    private final ResourceLinks links;
    private final BookService bookService;

    public PublisherController(PublisherService publisherService, ResourceLinks links, BookService bookService) {
        this.publisherService = publisherService;
        this.links = links;
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<EntityModel<Publisher>> createPublisher(@RequestBody Publisher publisher) {
        EntityModel<Publisher> created = links.publisher(publisherService.createPublisher(publisher));
        return ResponseEntity.created(created.getRequiredLink("self").toUri()).body(created);
    }

    @GetMapping("/{id}")
    public EntityModel<Publisher> getPublisherById(@PathVariable Long id) {
        return links.publisher(publisherService.getPublisherById(id)
                .orElseThrow(() -> new NoSuchElementException("Publisher not found")));
    }

    @GetMapping
    public CollectionModel<EntityModel<Publisher>> getAllPublishers() {
        return CollectionModel.of(publisherService.getAllPublishers().stream().map(links::publisher).toList(),
                linkTo(methodOn(PublisherController.class).getAllPublishers()).withSelfRel());
    }

    @GetMapping("/{id}/books")
    public CollectionModel<EntityModel<Book>> getBooksByPublisher(@PathVariable Long id) {
        return CollectionModel.of(bookService.getBooksByPublisher(id).stream().map(links::book).toList(),
                linkTo(methodOn(PublisherController.class).getBooksByPublisher(id)).withSelfRel(),
                linkTo(methodOn(PublisherController.class).getPublisherById(id)).withRel("publisher"));
    }

    @PutMapping("/{id}")
    public EntityModel<Publisher> updatePublisher(
            @PathVariable Long id,
            @RequestBody Publisher publisher) {
        return links.publisher(publisherService.updatePublisher(id, publisher));
    }

    @PatchMapping("/{id}")
    public EntityModel<Publisher> patchPublisher(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return links.publisher(publisherService.patchPublisher(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublisher(@PathVariable Long id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}
