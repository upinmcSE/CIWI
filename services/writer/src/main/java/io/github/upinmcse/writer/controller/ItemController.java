package io.github.upinmcse.writer.controller;

import io.github.upinmcse.common.model.Item;
import io.github.upinmcse.common.model.ItemValidator;
import io.github.upinmcse.writer.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository repository){
        this.itemRepository = repository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody(required = false) Item body) {
        List<String> errors = ItemValidator.validate(body);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        Item saved = itemRepository.insert(body);
        return ResponseEntity.created(URI.create("/api/v1/items/" + saved.id())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable long id) {
        return itemRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "not_found", "id", id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody(required = false) Item body) {
        List<String> errors = ItemValidator.validate(body);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        if (!itemRepository.update(id, body)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "not_found", "id", id));
        }
        return ResponseEntity.ok(itemRepository.findById(id).orElseThrow());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        if (!itemRepository.delete(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "not_found", "id", id));
        }
        return ResponseEntity.noContent().build();
    }
}
