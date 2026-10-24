package io.github.upinmcse.reader.controller;

import io.github.upinmcse.common.model.Item;
import io.github.upinmcse.reader.cache.ItemCache;
import io.github.upinmcse.reader.repository.ItemRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemCache itemCache;
    private final Counter hits;
    private final Counter misses;
    private final Counter notFound;

    public ItemController(ItemRepository itemRepository, ItemCache itemCache, MeterRegistry registry) {
        this.itemRepository     = itemRepository;
        this.itemCache          = itemCache;
        this.hits               = Counter.builder("reader.cache").tag("outcome", "hit").register(registry);
        this.misses             = Counter.builder("reader.cache").tag("outcome", "miss").register(registry);
        this.notFound           = Counter.builder("reader.cache").tag("outcome", "not_found").register(registry);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable long id) {
        Optional<Item> itemHitCache = itemCache.get(id);
        if (itemHitCache.isPresent()) {
            hits.increment();
            return ResponseEntity.ok().header("X-Cache", "HIT").body(itemHitCache.get());
        }
        Optional<Item> itemFromDb = itemRepository.findById(id);
        if (itemFromDb.isEmpty()) {
            notFound.increment();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header("X-Cache", "MISS")
                    .body(Map.of("error", "not_found", "id", id));
        }
        itemCache.put(itemFromDb.get());
        misses.increment();
        return ResponseEntity.ok().header("X-Cache", "MISS").body(itemFromDb.get());
    }
}
