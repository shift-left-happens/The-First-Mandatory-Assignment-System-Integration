package com.example.demo.request;

import tools.jackson.databind.JsonNode;
import java.util.Set;

public final class JsonFields {
    private JsonFields() {}

    public static void only(JsonNode body, Set<String> allowed) {
        if (body == null || !body.isObject()) {
            throw new IllegalArgumentException("Request body must be a JSON object");
        }
        body.properties().forEach(property -> {
            if (!allowed.contains(property.getKey())) {
                throw new IllegalArgumentException("Unknown field: " + property.getKey());
            }
        });
    }

    public static String text(JsonNode body, String field) {
        JsonNode value = body.get(field);
        if (value == null || !value.isTextual() || value.textValue().isBlank()) {
            throw new IllegalArgumentException(field + " must be a non-empty string");
        }
        return value.textValue();
    }

    public static Long longNumber(JsonNode body, String field) {
        JsonNode value = body.get(field);
        if (value == null || !value.isIntegralNumber() || !value.canConvertToLong()
                || value.longValue() <= 0) {
            throw new IllegalArgumentException(field + " must be a positive integer");
        }
        return value.longValue();
    }

    public static Integer year(JsonNode body) {
        JsonNode value = body.get("publishingYear");
        if (value == null || !value.isIntegralNumber() || !value.canConvertToInt()
                || value.intValue() < 1900) {
            throw new IllegalArgumentException("publishingYear must be an integer of at least 1900");
        }
        return value.intValue();
    }
}
