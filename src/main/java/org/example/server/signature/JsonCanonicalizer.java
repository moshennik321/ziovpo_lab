package org.example.server.signature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JsonCanonicalizer {

    private final ObjectMapper objectMapper;

    public JsonCanonicalizer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public String canonicalize(Object payload) {
        try {
            JsonNode node = objectMapper.valueToTree(payload);
            JsonNode normalized = normalize(node);
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException e) {
            throw new SignatureException("Failed to canonicalize JSON", e);
        }
    }

    public byte[] canonicalizeToUtf8Bytes(Object payload) {
        return canonicalize(payload).getBytes(StandardCharsets.UTF_8);
    }

    private JsonNode normalize(JsonNode node) {
        if (node == null || node.isNull()) {
            return NullNode.instance;
        }

        if (node.isObject()) {
            ObjectNode source = (ObjectNode) node;
            ObjectNode target = objectMapper.createObjectNode();

            List<String> fieldNames = new ArrayList<>();
            source.fieldNames().forEachRemaining(fieldNames::add);
            Collections.sort(fieldNames);

            for (String field : fieldNames) {
                target.set(field, normalize(source.get(field)));
            }
            return target;
        }

        if (node.isArray()) {
            ArrayNode source = (ArrayNode) node;
            ArrayNode target = objectMapper.createArrayNode();
            for (JsonNode item : source) {
                target.add(normalize(item));
            }
            return target;
        }

        return node;
    }
}