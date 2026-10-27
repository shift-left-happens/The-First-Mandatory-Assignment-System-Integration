package com.example.demo.request;

import tools.jackson.databind.JsonNode;
import java.util.Set;

public record BookPatchRequest(JsonNode body) {
    public static BookPatchRequest from(JsonNode body) {
        JsonFields.only(body, Set.of("title", "authorId", "publishingCompanyId", "publishingYear"));
        if (body.size() == 0) throw new IllegalArgumentException("PATCH body must contain a field");
        if (body.has("title")) JsonFields.text(body, "title");
        if (body.has("authorId")) JsonFields.longNumber(body, "authorId");
        if (body.has("publishingCompanyId")) JsonFields.longNumber(body, "publishingCompanyId");
        if (body.has("publishingYear")) JsonFields.year(body);
        return new BookPatchRequest(body);
    }
}
