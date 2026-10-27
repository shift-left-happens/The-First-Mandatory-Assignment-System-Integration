package com.example.demo.controller;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ApiController {
    @GetMapping("/api")
    public RepresentationModel<?> index() {
        return new RepresentationModel<>()
                .add(linkTo(methodOn(ApiController.class).index()).withSelfRel())
                .add(linkTo(methodOn(BookController.class).getAllBooks(null, null)).withRel("books"))
                .add(linkTo(methodOn(AuthorController.class).getAllAuthors()).withRel("authors"))
                .add(linkTo(methodOn(PublisherController.class).getAllPublishers()).withRel("publishers"));
    }
}
