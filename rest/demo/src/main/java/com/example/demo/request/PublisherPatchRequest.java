package com.example.demo.request;

import tools.jackson.databind.JsonNode;
import java.util.Set;

public record PublisherPatchRequest(JsonNode body) {
    public static PublisherPatchRequest from(JsonNode body) {
        JsonFields.only(body, Set.of("name"));
        if (body.size() == 0) throw new IllegalArgumentException("PATCH body must contain a field");
        if (body.has("name")) JsonFields.text(body, "name");
        return new PublisherPatchRequest(body);
    }
}
