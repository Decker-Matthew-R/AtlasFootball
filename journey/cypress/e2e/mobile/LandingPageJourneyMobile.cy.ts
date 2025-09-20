import {mockFixturesResponse} from '../../support/mockData/fixturesData';

export {}

describe("As a mobile user on the landing page", () => {
  beforeEach(() => {
    cy.viewport(375, 667);
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit('/');
    cy.wait('@getFixtures');
    cy.wait(2000);
  })

  afterEach(() => {
    Cypress.session.clearAllSavedSessions();
  })

  it('I should see a mobile desktop screen with the correct dimensions of 375 x 667', () => {
    cy.visit('/');

    cy.window().then((win) => {
      expect(win.innerWidth).to.equal(375);
      expect(win.innerHeight).to.equal(667);
    });

    const title = cy.title();
    title.should('contain', 'Atlas');

    const backgroundImage = cy.get('[data-testid="landing-page-container"]');
    backgroundImage.should('be.visible').should('have.css', 'background-image')
  });

  it('I should be able to see a navbar with humburger menu and profile button', () => {
    cy.visit('/');

    cy.get('[aria-label="atlas-site-name-mobile"]').should('be.visible');
    cy.get('[aria-label="atlas-logo-mobile"]').should('be.visible');
    cy.get('[aria-label="navigation-links"]').should('be.visible');
    cy.get('[aria-label="Open Profile Settings"]').should('be.visible');
  });

  it('I should see a hamburger menu with navigation options', () => {

    cy.get('[aria-label="navigation-links"]').as('hamburgerMenu');
    cy.get('@hamburgerMenu').should('be.visible');

    cy.get('@hamburgerMenu').click();

    cy.findAllByRole('menuitem').as('navigationMenuItems');

    cy.get('@navigationMenuItems').should('be.visible');
    cy.get('@navigationMenuItems').eq(0).should('be.visible').should('contain.text', 'News');
    cy.get('@navigationMenuItems').eq(1).should('be.visible').should('contain.text', 'Matches');

    cy.get('body').click();

    cy.get('@navigationMenuItems').should('not.exist')
  });

  it('I should be able to click the profile icon and see login button when unauthenticated', () => {

    const profileButton = cy.findByRole('button', { name: 'Open Profile Settings' });

    profileButton.click();

    cy.findByRole('menu').as('profileMenuItems');
    cy.get('@profileMenuItems').should('be.visible');
    cy.get('@profileMenuItems').eq(0).should('be.visible').should('contain.text', 'Login');

    cy.get('body').click();

    cy.get('@profileMenuItems').should('not.exist')
  });

  it('I should be able to click the profile icon and see profile settings in mobile when authenticated', () => {
    cy.loginViaBackend({
      email: 'authenticated-test@example.com',
      name: 'Authenticated User'
    });

    cy.visit('/');

    const profileButtonMobile = cy.findByRole('button', { name: 'Open Profile Settings' });

    profileButtonMobile.click();

    cy.findAllByRole('menuitem').as('profileMenuItemsMobile');
    cy.get('@profileMenuItemsMobile').should('be.visible');
    cy.get('@profileMenuItemsMobile').eq(0).should('be.visible').should('contain.text', 'Profile');
    cy.get('@profileMenuItemsMobile').eq(1).should('be.visible').should('contain.text', 'Logout');

    cy.get('body').click();

    cy.get('@profileMenuItemsMobile').should('not.exist')
  });

  it('should load and display fixtures from different leagues', () => {

    cy.get('[data-testid="landing-page-container"]').should('be.visible');
    cy.contains('Premier League').should('be.visible');
    cy.contains('England • 2024 • Regular Season - 20').should('be.visible');
    cy.contains('Arsenal').should('be.visible');
    cy.contains('Manchester City').should('be.visible');

    cy.contains('Barcelona').scrollIntoView();
    cy.wait(1000);

    cy.contains('La Liga').should('be.visible');
    cy.contains('Spain • 2024 • Regular Season - 20').should('be.visible');
    cy.contains('Barcelona').should('be.visible');
    cy.contains('Real Madrid').should('be.visible');
  });

  it('should handle API errors gracefully', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 500,
      body: {error: 'Internal server error'}
    }).as('getFixturesError');

    cy.visit('/');
    cy.wait('@getFixturesError');

    cy.contains('No fixtures available at the moment.').should('be.visible');
  });

  it('should handle empty fixtures response', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: {
        results: 0,
        status: "success",
        message: "No fixtures found",
        fixtures: []
      }
    }).as('getEmptyFixtures');

    cy.visit('/');
    cy.wait('@getEmptyFixtures');

    cy.contains('No fixtures available at the moment.').should('be.visible');
  });

  it('should display league carousels with scroll functionality', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit('/');
    cy.wait('@getFixtures');
    cy.wait(2000);

    cy.contains('Premier League').should('be.visible');

    cy.get('[aria-label="Scroll left"]').should('exist');
    cy.get('[aria-label="Scroll right"]').should('exist');

    cy.get('[aria-label="Scroll right"]').first().should('be.visible').and('not.be.disabled');
    cy.get('[aria-label="Scroll left"]').first().should('be.visible').and('be.disabled');

    for (let i = 0; i < 7; i++) {
      cy.get('[aria-label="Scroll right"]').first().click();
      cy.wait(500);
    }

    cy.get('[aria-label="Scroll right"]').first().should('be.disabled');
    cy.get('[aria-label="Scroll left"]').first().should('not.be.disabled');

    cy.get('[aria-label="Scroll left"]').first().click();
    cy.wait(500);

    cy.get('[aria-label="Scroll right"]').first().should('not.be.disabled');
    cy.get('[aria-label="Scroll left"]').first().should('not.be.disabled');
  });
});
