package com.example.demo.request;

import com.example.demo.model.Publisher;
import tools.jackson.databind.JsonNode;
import java.util.Set;

public record PublisherRequest(String name) {
    public static PublisherRequest from(JsonNode body) {
        JsonFields.only(body, Set.of("name"));
        return new PublisherRequest(JsonFields.text(body, "name"));
    }

    public Publisher toEntity() {
        return new Publisher(name);
    }
}
