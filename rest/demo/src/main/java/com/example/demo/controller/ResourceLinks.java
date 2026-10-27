package com.example.demo.controller;

import com.example.demo.model.Author;
import com.example.demo.model.Book;
import com.example.demo.model.Publisher;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ResourceLinks {
    public EntityModel<Book> book(Book book) {
        return EntityModel.of(book,
                linkTo(methodOn(BookController.class).getBookById(book.getId())).withSelfRel(),
                linkTo(methodOn(BookController.class).getAllBooks(null, null)).withRel("books"),
                linkTo(methodOn(AuthorController.class).getAuthorById(book.getAuthorId())).withRel("author"),
                linkTo(methodOn(PublisherController.class).getPublisherById(book.getPublishingCompanyId())).withRel("publisher"));
    }

    public EntityModel<Author> author(Author author) {
        return EntityModel.of(author,
                linkTo(methodOn(AuthorController.class).getAuthorById(author.getId())).withSelfRel(),
                linkTo(methodOn(AuthorController.class).getAllAuthors()).withRel("authors"),
                linkTo(methodOn(AuthorController.class).getBooksByAuthor(author.getId()))
                        .withRel("books"));
    }

    public EntityModel<Publisher> publisher(Publisher publisher) {
        return EntityModel.of(publisher,
                linkTo(methodOn(PublisherController.class).getPublisherById(publisher.getId())).withSelfRel(),
                linkTo(methodOn(PublisherController.class).getAllPublishers()).withRel("publishers"),
                linkTo(methodOn(PublisherController.class).getBooksByPublisher(publisher.getId()))
                        .withRel("books"));
    }
}
