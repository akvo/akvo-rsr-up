
Feature: Create update
  In order to make it simple to create project updates
  As a RSR project editor
  I want to be able to do project updates on a mobile device

  Scenario: Create project update with valid GPS position and connection
    Given context
    When event
    Then outcome
  
  Scenario: Create project update with invalid GPS position and connection
    Given context
    When event
    Then outcome

  Scenario: Create project update with valid GPS position and no conneciton
    Given context
    When event
    Then outcome

  Scenario: Create project update with invalid GPS posision and no connection
    Given context
    When event
    Then outcome
