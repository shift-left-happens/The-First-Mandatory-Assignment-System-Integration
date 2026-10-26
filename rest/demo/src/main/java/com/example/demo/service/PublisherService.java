
package com.example.demo.service;

import com.example.demo.model.Publisher;
import com.example.demo.repository.PublisherRepository;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
                .orElseThrow(() -> new RuntimeException("Publisher not found"));

        publisher.setName(updatedPublisher.getName());

        return publisherRepository.save(publisher);
    }

    public Publisher patchPublisher(Long id, Map<String, Object> updates) {

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found"));

        if (updates.containsKey("name")) {
            publisher.setName((String) updates.get("name"));
        }

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
}
