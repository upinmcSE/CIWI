package io.github.upinmcse.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Item(
        Long id,
        String name,
        String description,
        long priceCents,
        String currency,
        Integer stock,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int MAX_NAME_LEN = 200;
    public static final int MAX_DESCRIPTION_LEN = 2000;

    public Item withId(long newId) {
        return new Item(newId, name, description, priceCents, currency, stock, createdAt, updatedAt);
    }

    public Item withTimestamps(Instant created, Instant updated) {
        return new Item(id, name, description, priceCents, currency, stock, created, updated);
    }
}