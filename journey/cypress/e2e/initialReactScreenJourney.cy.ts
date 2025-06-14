export {}

describe("As a user", () => {
  afterEach(() => {
    Cypress.session.clearAllSavedSessions();
  })
  it('I should be able to click the count button and see the counter increase', () => {
    cy.visit("/");
  });

})
