package com.example.demo.request;

import com.example.demo.model.Book;
import tools.jackson.databind.JsonNode;
import java.util.Set;

public record BookRequest(String title, Long authorId, Long publishingCompanyId, Integer publishingYear) {
    public static BookRequest from(JsonNode body) {
        JsonFields.only(body, Set.of("title", "authorId", "publishingCompanyId", "publishingYear"));
        return new BookRequest(JsonFields.text(body, "title"),
                JsonFields.longNumber(body, "authorId"),
                JsonFields.longNumber(body, "publishingCompanyId"), JsonFields.year(body));
    }

    public Book toEntity() {
        return new Book(title, authorId, publishingCompanyId, publishingYear);
    }
}
