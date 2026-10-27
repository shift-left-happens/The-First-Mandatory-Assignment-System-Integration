package com.example.demo.request;

import tools.jackson.databind.JsonNode;
import java.util.Set;

public record AuthorPatchRequest(JsonNode body) {
    public static AuthorPatchRequest from(JsonNode body) {
        JsonFields.only(body, Set.of("name", "surname"));
        if (body.size() == 0) throw new IllegalArgumentException("PATCH body must contain a field");
        if (body.has("name")) JsonFields.text(body, "name");
        if (body.has("surname")) JsonFields.text(body, "surname");
        return new AuthorPatchRequest(body);
    }
}
