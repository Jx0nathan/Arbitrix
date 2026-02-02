package io.arbitrix.core.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSON utility class using Jackson
 */
@Slf4j
public final class JacksonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private JacksonUtil() {
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJsonStr(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON", e);
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON to object", e);
            return null;
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON to object", e);
            return null;
        }
    }

    public static <T> List<T> fromList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON to list", e);
            return Collections.emptyList();
        }
    }

    public static <V> Map<String, V> fromMap(String json, Class<V> valueClass) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, String.class, valueClass));
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON to map", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Alias for fromJson - for backward compatibility
     */
    public static <T> T from(String json, Class<T> clazz) {
        return fromJson(json, clazz);
    }

    /**
     * Alias for fromJson with TypeReference - for backward compatibility
     */
    public static <T> T from(String json, TypeReference<T> typeReference) {
        return fromJson(json, typeReference);
    }

    /**
     * Alias for fromJson - for backward compatibility with toObj naming
     */
    public static <T> T toObj(String json, Class<T> clazz) {
        return fromJson(json, clazz);
    }

    /**
     * Parse JSON to Map with Object values
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON to map", e);
            return Collections.emptyMap();
        }
    }
}
