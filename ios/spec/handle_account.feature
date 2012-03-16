
Feature: Handle RSR account on app
  In order to create project updates from the application
  As a RSR project editor
  I want to be able to authenticate my apps.
 
  Scenario: Unscucessful account setup
    Given I start an unauthenticated application
    And I provide invalid credentials when promted
    Then I should not be authenticated
    
  Scenario: Successful account setup
    Given I have a valid RSR account
    And I start an unauthenticated application
    And I sign in with valid credentials when prompted
    Then I should be authenticated
  
  Scenario: Further launches of the app
    Given I have successfuly authenticated the app 
    When start the application
    Then I should not be prompted for authentication
  
  Scenario: Invalidate session
    Given I have successfuly authenticated the app
    When I choose to sign out
    Then I should be prompted for new credentials
