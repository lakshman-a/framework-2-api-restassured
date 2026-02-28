package com.upskill.api;

import com.upskill.config.ConfigManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ApiClient - Central class for making HTTP requests using REST Assured.
 *
 * Provides methods for all HTTP verbs (GET, POST, PUT, PATCH, DELETE).
 * Integrates with Allure for request/response reporting.
 * Handles auth, headers, params, and body.
 */
public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);

    /**
     * Get a base request spec pre-configured with base URL, content type, and Allure filter.
     */
    public static RequestSpecification getRequestSpec(String baseUrl) {
        return RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(new AllureRestAssured())  // Allure captures request/response
                .log().all();  // Log everything to console
    }

    /**
     * Get spec with the default base URL from config.
     */
    public static RequestSpecification getRequestSpec() {
        return getRequestSpec(ConfigManager.get("api.base.url"));
    }

    /**
     * Get spec for the reqres.in API.
     */
    public static RequestSpecification getReqresSpec() {
        return getRequestSpec(ConfigManager.get("api.reqres.base.url"));
    }

    // ==================== HTTP Methods ====================

    @Step("GET {endpoint}")
    public static Response get(String endpoint) {
        log.info("GET {}", endpoint);
        Response response = getRequestSpec()
                .when()
                .get(endpoint);
        logResponse(response);
        return response;
    }

    @Step("GET {endpoint} with query params")
    public static Response get(String endpoint, Map<String, Object> queryParams) {
        log.info("GET {} with params: {}", endpoint, queryParams);
        Response response = getRequestSpec()
                .queryParams(queryParams)
                .when()
                .get(endpoint);
        logResponse(response);
        return response;
    }

    @Step("GET {endpoint} with headers")
    public static Response getWithHeaders(String endpoint, Map<String, String> headers) {
        log.info("GET {} with headers: {}", endpoint, headers);
        Response response = getRequestSpec()
                .headers(headers)
                .when()
                .get(endpoint);
        logResponse(response);
        return response;
    }

    @Step("POST {endpoint}")
    public static Response post(String endpoint, Object body) {
        log.info("POST {} with body: {}", endpoint, body);
        Response response = getRequestSpec()
                .body(body)
                .when()
                .post(endpoint);
        logResponse(response);
        return response;
    }

    @Step("POST {endpoint} to reqres API")
    public static Response postReqres(String endpoint, Object body) {
        log.info("POST (reqres) {} with body: {}", endpoint, body);
        Response response = getReqresSpec()
                .body(body)
                .when()
                .post(endpoint);
        logResponse(response);
        return response;
    }

    @Step("PUT {endpoint}")
    public static Response put(String endpoint, Object body) {
        log.info("PUT {} with body: {}", endpoint, body);
        Response response = getRequestSpec()
                .body(body)
                .when()
                .put(endpoint);
        logResponse(response);
        return response;
    }

    @Step("PATCH {endpoint}")
    public static Response patch(String endpoint, Object body) {
        log.info("PATCH {} with body: {}", endpoint, body);
        Response response = getRequestSpec()
                .body(body)
                .when()
                .patch(endpoint);
        logResponse(response);
        return response;
    }

    @Step("DELETE {endpoint}")
    public static Response delete(String endpoint) {
        log.info("DELETE {}", endpoint);
        Response response = getRequestSpec()
                .when()
                .delete(endpoint);
        logResponse(response);
        return response;
    }

    @Step("POST {endpoint} with Bearer Token auth")
    public static Response postWithAuth(String endpoint, Object body, String token) {
        log.info("POST (auth) {} with token", endpoint);
        Response response = getRequestSpec()
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post(endpoint);
        logResponse(response);
        return response;
    }

    @Step("GET {endpoint} with Basic Auth")
    public static Response getWithBasicAuth(String endpoint, String username, String password) {
        log.info("GET (basic auth) {}", endpoint);
        Response response = getRequestSpec()
                .auth().basic(username, password)
                .when()
                .get(endpoint);
        logResponse(response);
        return response;
    }

    // ==================== Helpers ====================

    private static void logResponse(Response response) {
        log.info("Response Status: {} {}", response.getStatusCode(), response.getStatusLine());
        log.debug("Response Body: {}", response.getBody().asPrettyString());

        // Attach response to Allure
        Allure.addAttachment("Response Body", "application/json", response.getBody().asPrettyString());
    }
}
