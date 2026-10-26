
package com.example.demo.controller;

import com.example.demo.model.Publisher;
import com.example.demo.service.PublisherService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping
    public ResponseEntity<Publisher> createPublisher(@RequestBody Publisher publisher) {
        Publisher createdPublisher = publisherService.createPublisher(publisher);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPublisher);
    }

    @GetMapping("/{id}")
    public Publisher getPublisherById(@PathVariable Long id) {
        return publisherService.getPublisherById(id)
                .orElseThrow(() -> new NoSuchElementException("Publisher not found"));
    }

    @GetMapping
    public List<Publisher> getAllPublishers() {
        return publisherService.getAllPublishers();
    }

    @PutMapping("/{id}")
    public Publisher updatePublisher(
            @PathVariable Long id,
            @RequestBody Publisher publisher) {
        return publisherService.updatePublisher(id, publisher);
    }

    @PatchMapping("/{id}")
    public Publisher patchPublisher(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return publisherService.patchPublisher(id, updates);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublisher(@PathVariable Long id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}
