package com.upskill.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * JsonUtils - Helper methods for JSON operations.
 */
public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Convert an object to JSON string.
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Convert JSON string to an object.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract a field value from a Response using JsonPath.
     */
    public static <T> T extractField(Response response, String jsonPath) {
        return response.jsonPath().get(jsonPath);
    }

    /**
     * Extract a list from a Response.
     */
    public static <T> List<T> extractList(Response response, String jsonPath) {
        return response.jsonPath().getList(jsonPath);
    }

    /**
     * Pretty print a Response body.
     */
    public static String prettyPrint(Response response) {
        return response.getBody().asPrettyString();
    }
}
