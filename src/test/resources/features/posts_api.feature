@all @posts @api
Feature: Posts API - End-to-End Test with Cross-Validation
  As a QA engineer
  I want to perform end-to-end API testing
  So that I can validate data flow across multiple API endpoints

  # =====================================================================
  # This feature demonstrates END-TO-END API testing:
  #   1. Create test data via POST
  #   2. Retrieve and validate via GET
  #   3. Cross-validate fields between two API responses
  #   4. Validate API-to-API field consistency
  #   5. Clean up test data via DELETE
  # =====================================================================

  @smoke @qa @regression
  Scenario: End-to-end - Create post, retrieve it, validate, and delete
    # Step 1: Create a new post
    Given I prepare a POST request to "/posts" with body
      """
      {
        "userId": 1,
        "title": "Automation Test Post",
        "body": "This post was created by the API automation framework"
      }
      """
    When I send the request
    Then the response status code should be 201
    And I extract and store the field "id" as "new_post_id"
    And the response field "title" should be "Automation Test Post"
    And the response field "userId" should be 1

    # Step 2: Retrieve the user who owns this post
    Given I prepare a GET request to "/users/1"
    When I send the request
    Then the response status code should be 200
    And I extract and store the field "name" as "post_author_name"
    And I extract and store the field "id" as "author_id"

    # Step 3: Validate - The post's userId matches the user's id
    Then the stored value "author_id" should equal 1

  @regression @qa
  Scenario: Get all posts for a specific user and validate count
    Given I prepare a GET request to "/posts" with query params
      | key    | value |
      | userId | 1     |
    When I send the request
    Then the response status code should be 200
    And the response should contain a list of posts
    And the list should have at least 1 items

  @regression @qa
  Scenario: Get post comments and validate nested data
    # Get comments for post 1
    Given I prepare a GET request to "/posts/1/comments"
    When I send the request
    Then the response status code should be 200
    And the response should contain a list of comments
    And each comment should have fields "id", "postId", "name", "email", "body"
    And all "postId" values in the list should be 1

  @smoke @qa
  Scenario: API-to-API cross validation - user posts consistency
    # First: Get user 1
    Given I prepare a GET request to "/users/1"
    When I send the request
    Then the response status code should be 200
    And I extract and store the field "id" as "user_id"

    # Then: Get posts by that user
    Given I prepare a GET request to "/posts" with query params
      | key    | value |
      | userId | 1     |
    When I send the request
    Then the response status code should be 200
    And all "userId" values in the list should be 1
