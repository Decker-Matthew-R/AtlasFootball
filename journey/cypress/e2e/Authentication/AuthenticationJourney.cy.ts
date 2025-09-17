import {mockFixturesResponse} from '../../support/mockData/fixturesData';
export {}

describe('Minimal Backend Auth Test', () => {
  it('should login via backend and verify cookies', () => {

    cy.clearAllCookies()

    cy.loginViaBackend({
      email: 'test@example.com',
      name: 'Test User'
    })

    cy.getCookie('jwt').should('exist')
    cy.getCookie('user_info').should('exist')

    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit('/');
    cy.wait('@getFixtures');
    cy.wait(2000);

    cy.getCookie('jwt').should('exist')
    cy.getCookie('user_info').should('exist')

    cy.log('✅ Backend authentication test passed!')
  })
})
