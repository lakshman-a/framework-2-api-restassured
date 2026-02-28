package com.upskill.stepdefinitions;

import com.upskill.api.ApiClient;
import com.upskill.utils.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ApiSteps {

    private static final Logger log = LoggerFactory.getLogger(ApiSteps.class);

    @Given("I prepare a GET request to {string}")
    public void iPrepareAGetRequestTo(String endpoint) {
        TestContext.set("method", "GET");
        TestContext.set("endpoint", endpoint);
        TestContext.set("queryParams", null);
        TestContext.set("body", null);
    }

    @Given("I prepare a GET request to {string} with query params")
    public void iPrepareAGetRequestWithQueryParams(String endpoint, DataTable dataTable) {
        TestContext.set("method", "GET");
        TestContext.set("endpoint", endpoint);
        Map<String, Object> params = new HashMap<>();
        for (Map<String, String> row : dataTable.asMaps(String.class, String.class)) {
            params.put(row.get("key"), row.get("value"));
        }
        TestContext.set("queryParams", params);
    }

    @Given("I prepare a POST request to {string} with body")
    public void iPrepareAPostRequestWithBody(String endpoint, String body) {
        TestContext.set("method", "POST");
        TestContext.set("endpoint", endpoint);
        TestContext.set("body", body);
    }

    @Given("I prepare a PUT request to {string} with body")
    public void iPrepareAPutRequestWithBody(String endpoint, String body) {
        TestContext.set("method", "PUT");
        TestContext.set("endpoint", endpoint);
        TestContext.set("body", body);
    }

    @Given("I prepare a PATCH request to {string} with body")
    public void iPrepareAPatchRequestWithBody(String endpoint, String body) {
        TestContext.set("method", "PATCH");
        TestContext.set("endpoint", endpoint);
        TestContext.set("body", body);
    }

    @Given("I prepare a DELETE request to {string}")
    public void iPrepareADeleteRequestTo(String endpoint) {
        TestContext.set("method", "DELETE");
        TestContext.set("endpoint", endpoint);
    }

    @When("I send the request")
    public void iSendTheRequest() {
        String method = TestContext.get("method");
        String endpoint = TestContext.get("endpoint");
        String body = TestContext.get("body");
        Map<String, Object> queryParams = TestContext.get("queryParams");

        log.info("Sending {} request to {}", method, endpoint);
        Response response;
        switch (method) {
            case "GET":
                response = (queryParams != null) ? ApiClient.get(endpoint, queryParams) : ApiClient.get(endpoint);
                break;
            case "POST":
                response = ApiClient.post(endpoint, body);
                break;
            case "PUT":
                response = ApiClient.put(endpoint, body);
                break;
            case "PATCH":
                response = ApiClient.patch(endpoint, body);
                break;
            case "DELETE":
                response = ApiClient.delete(endpoint);
                break;
            default:
                throw new IllegalArgumentException("Unknown HTTP method: " + method);
        }
        TestContext.setResponse(response);
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expected) {
        assertEquals("HTTP Status Code", expected, TestContext.getResponse().getStatusCode());
    }

    @Then("the response field {string} should be {string}")
    public void theResponseFieldShouldBeString(String field, String expected) {
        String actual = TestContext.getResponse().jsonPath().getString(field);
        assertEquals("Field: " + field, expected, actual);
    }

    @Then("the response field {string} should be {int}")
    public void theResponseFieldShouldBeInt(String field, int expected) {
        int actual = TestContext.getResponse().jsonPath().getInt(field);
        assertEquals("Field: " + field, expected, actual);
    }

    @Then("the response field {string} should not be null")
    public void theResponseFieldShouldNotBeNull(String field) {
        assertNotNull("Field '" + field + "' should not be null",
                TestContext.getResponse().jsonPath().get(field));
    }

    @And("I extract and store the field {string} as {string}")
    public void iExtractAndStoreTheField(String field, String key) {
        Object value = TestContext.getResponse().jsonPath().get(field);
        log.info("Extracted '{}' = {} and stored as '{}'", field, value, key);
        TestContext.set(key, value);
    }

    @Then("the stored value {string} should equal {int}")
    public void theStoredValueShouldEqual(String key, int expected) {
        Object val = TestContext.get(key);
        assertEquals("Stored value '" + key + "'", expected, ((Number) val).intValue());
    }

    @Then("the response should contain a list of users")
    public void theResponseShouldContainAListOfUsers() {
        List<?> list = TestContext.getResponse().jsonPath().getList("$");
        assertNotNull("Response should be a list", list);
        assertFalse("User list should not be empty", list.isEmpty());
        log.info("Response contains {} users", list.size());
    }

    @Then("the response should contain a list of posts")
    public void theResponseShouldContainAListOfPosts() {
        List<?> list = TestContext.getResponse().jsonPath().getList("$");
        assertNotNull("Response should be a list", list);
        assertFalse("Post list should not be empty", list.isEmpty());
    }

    @Then("the response should contain a list of comments")
    public void theResponseShouldContainAListOfComments() {
        List<?> list = TestContext.getResponse().jsonPath().getList("$");
        assertNotNull("Response should be a list", list);
        assertFalse("Comment list should not be empty", list.isEmpty());
    }

    @Then("the list should have at least {int} items")
    public void theListShouldHaveAtLeastItems(int min) {
        List<?> list = TestContext.getResponse().jsonPath().getList("$");
        assertTrue("List should have >= " + min + " items, got " + list.size(), list.size() >= min);
    }

    @Then("each user should have fields {string}, {string}, {string}, {string}")
    public void eachUserShouldHaveFields(String f1, String f2, String f3, String f4) {
        List<Map<String, Object>> items = TestContext.getResponse().jsonPath().getList("$");
        for (Map<String, Object> item : items) {
            assertNotNull("Missing: " + f1, item.get(f1));
            assertNotNull("Missing: " + f2, item.get(f2));
            assertNotNull("Missing: " + f3, item.get(f3));
            assertNotNull("Missing: " + f4, item.get(f4));
        }
    }

    @Then("each comment should have fields {string}, {string}, {string}, {string}, {string}")
    public void eachCommentShouldHaveFields(String f1, String f2, String f3, String f4, String f5) {
        List<Map<String, Object>> items = TestContext.getResponse().jsonPath().getList("$");
        for (Map<String, Object> item : items) {
            assertNotNull("Missing: " + f1, item.get(f1));
            assertNotNull("Missing: " + f2, item.get(f2));
            assertNotNull("Missing: " + f3, item.get(f3));
            assertNotNull("Missing: " + f4, item.get(f4));
            assertNotNull("Missing: " + f5, item.get(f5));
        }
    }

    @Then("all {string} values in the list should be {int}")
    public void allFieldValuesInTheListShouldBe(String field, int expected) {
        List<Integer> values = TestContext.getResponse().jsonPath().getList(field, Integer.class);
        for (Integer val : values) {
            assertEquals("Field '" + field + "' mismatch", expected, val.intValue());
        }
    }

    @And("the first user's {string} should be {string}")
    public void theFirstUserFieldShouldBe(String field, String expected) {
        String actual = TestContext.getResponse().jsonPath().getString("[0]." + field);
        assertEquals("First user's " + field, expected, actual);
    }

    @Then("the response time should be less than {int} milliseconds")
    public void theResponseTimeShouldBeLessThan(int maxMs) {
        long actual = TestContext.getResponse().getTime();
        log.info("Response time: {}ms (max allowed: {}ms)", actual, maxMs);
        assertTrue("Response time " + actual + "ms exceeded " + maxMs + "ms", actual < maxMs);
    }
}
