
package com.example.demo.service;

import com.example.demo.model.Publisher;
import com.example.demo.request.PublisherPatchRequest;
import com.example.demo.repository.PublisherRepository;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;

    public PublisherService(PublisherRepository publisherRepository, BookRepository bookRepository) {
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
    }

    public Publisher createPublisher(Publisher publisher) {
        validatePublisher(publisher);
        return publisherRepository.save(publisher);
    }

    public Optional<Publisher> getPublisherById(Long id) {
        return publisherRepository.findById(id);
    }

    public List<Publisher> getAllPublishers() {
        return publisherRepository.findAll();
    }

    public Publisher updatePublisher(Long id, Publisher updatedPublisher) {

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Publisher not found"));

        validatePublisher(updatedPublisher);

        publisher.setName(updatedPublisher.getName());

        validatePublisher(publisher);
        return publisherRepository.save(publisher);
    }

    public Publisher patchPublisher(Long id, PublisherPatchRequest updates) {

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Publisher not found"));

        Publisher candidate = new Publisher(publisher.getName());
        if (updates.body().has("name")) candidate.setName(updates.body().get("name").textValue());
        validatePublisher(candidate);
        publisher.setName(candidate.getName());
        return publisherRepository.save(publisher);
    }

    public void deletePublisher(Long id) {

        if (!publisherRepository.existsById(id)) {
            throw new NoSuchElementException("Publisher not found");
        }

        if (bookRepository.existsByPublishingCompanyId(id)) {
            throw new IllegalStateException(
                    "Cannot delete publisher because it is referenced by existing books");
        }

        publisherRepository.deleteById(id);
    }

    private void validatePublisher(Publisher publisher) {
        if (publisher == null || publisher.getName() == null || publisher.getName().isBlank()) {
            throw new IllegalArgumentException("Publisher name is required");
        }
    }
}
