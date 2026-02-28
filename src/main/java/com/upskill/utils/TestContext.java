package com.upskill.utils;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * TestContext - Shares state across Cucumber step definitions within a scenario.
 *
 * Since Cucumber creates new step definition instances, we use this
 * ThreadLocal context to share data like responses, extracted fields, etc.
 */
public class TestContext {

    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    public static void set(String key, Object value) {
        context.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) context.get().get(key);
    }

    public static Response getResponse() {
        return get("response");
    }

    public static void setResponse(Response response) {
        set("response", response);
    }

    public static void clear() {
        context.get().clear();
    }

    public static void remove() {
        context.remove();
    }
}
