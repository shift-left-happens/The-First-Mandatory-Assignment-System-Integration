package com.example.demo.request;

import com.example.demo.model.Author;
import tools.jackson.databind.JsonNode;
import java.util.Set;

public record AuthorRequest(String name, String surname) {
    public static AuthorRequest from(JsonNode body) {
        JsonFields.only(body, Set.of("name", "surname"));
        return new AuthorRequest(JsonFields.text(body, "name"), JsonFields.text(body, "surname"));
    }

    public Author toEntity() {
        return new Author(name, surname);
    }
}
