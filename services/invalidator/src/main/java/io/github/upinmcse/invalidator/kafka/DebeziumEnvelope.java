package io.github.upinmcse.invalidator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public final class DebeziumEnvelope {
    public enum Op { CREATE, UPDATE, DELETE, READ, TRUNCATE, UNKNOWN }

    private final Op op;
    private final JsonNode beforeOrAfter;

    private DebeziumEnvelope(Op op, JsonNode beforeOrAfter) {
        this.op = op;
        this.beforeOrAfter = beforeOrAfter;
    }

    public Op op() {
        return op;
    }

    public Optional<Long> primaryKey(String column) {
        if (beforeOrAfter == null || beforeOrAfter.isMissingNode() || beforeOrAfter.isNull()) {
            return Optional.empty();
        }
        JsonNode pk = beforeOrAfter.path(column);
        return pk.isNumber() ? Optional.of(pk.asLong()) : Optional.empty();
    }

    public static Optional<DebeziumEnvelope> parse(String json, ObjectMapper mapper) {
        if (json == null || json.isBlank()) return Optional.empty();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode payload = root.has("payload") ? root.get("payload") : root;
            String opCode = payload.path("op").asText("");
            Op op = switch (opCode) {
                case "c" -> Op.CREATE;
                case "u" -> Op.UPDATE;
                case "d" -> Op.DELETE;
                case "r" -> Op.READ;
                case "t" -> Op.TRUNCATE;
                default  -> Op.UNKNOWN;
            };
            JsonNode body = (op == Op.DELETE) ? payload.path("before") : payload.path("after");
            return Optional.of(new DebeziumEnvelope(op, body));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
