import {mockFixturesResponse} from '../../support/mockData/fixturesData';

export {}

describe("As a desktop user, I should see fixtures on the landing page", () => {

  beforeEach(() => {
    cy.viewport(1920, 1080);
  });

  afterEach(() => {
    Cypress.session.clearAllSavedSessions();
  });

  it('I should see a mobile desktop screen with the correct dimensions of 1920 x 1080', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit('/');
    cy.wait('@getFixtures');
    cy.wait(2000);

    cy.window().then((win) => {
      expect(win.innerWidth).to.equal(1920);
      expect(win.innerHeight).to.equal(1080);
    });

    const title = cy.title();
    title.should('contain', 'Atlas');

    const backgroundImage = cy.get('[data-testid="landing-page-container"]');
    backgroundImage.should('be.visible').should('have.css', 'background-image')
  });

  it('I should be able to see a navbar with buttons', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit('/');
    cy.wait('@getFixtures');
    cy.wait(2000);

    const siteNameOnNavBar = cy.get('[aria-label="atlas-site-name"]');
    siteNameOnNavBar.should('be.visible').and('contain.text', 'ATLAS');

    const siteLogoDesktop = cy.get('[aria-label="atlas-logo"]');
    siteLogoDesktop.should('be.visible');


    const newsButton = cy.findByRole('button', {name: 'News'});
    newsButton.should('be.visible');

    const matchesButton = cy.findByRole('button', {name: 'Matches'});
    matchesButton.should('be.visible');

    const profileButton = cy.findByRole('button', {name: 'Open Profile Settings'});
    profileButton.should('be.visible');
  });


  it('I should be able to click the profile icon and see login button when unauthenticated', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit("/")

    const profileButton = cy.findByRole('button', {name: 'Open Profile Settings'});

    profileButton.click();

    cy.findByRole('menu').as('profileMenuItems');
    cy.get('@profileMenuItems').should('be.visible');
    cy.get('@profileMenuItems').eq(0).should('be.visible').should('contain.text', 'Login');


    cy.get('body').click();

    cy.get('@profileMenuItems').should('not.exist')
  });

  it('I should be able to click the profile icon and see profile settings when authenticated', () => {
    cy.loginViaBackend({
      email: 'authenticated-test@example.com',
      name: 'Authenticated User'
    });

    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit('/');
    cy.wait('@getFixtures');
    cy.wait(2000);

    const profileButton = cy.findByRole('button', {name: 'Open Profile Settings'});

    profileButton.click();

    cy.findAllByRole('menuitem').as('profileMenuItems');
    cy.get('@profileMenuItems').should('be.visible');
    cy.get('@profileMenuItems').eq(0).should('be.visible').should('contain.text', 'Profile');
    cy.get('@profileMenuItems').eq(1).should('be.visible').should('contain.text', 'Logout');

    cy.get('body').click();

    cy.get('@profileMenuItems').should('not.exist')
  });

  it('should load and display fixtures from different leagues', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit('/');
    cy.wait('@getFixtures');
    cy.wait(2000);

    cy.get('[data-testid="landing-page-container"]').should('be.visible');

    cy.contains('Premier League').should('be.visible');
    cy.contains('La Liga').should('be.visible');

    cy.contains('England • 2024 • Regular Season - 20').should('be.visible');
    cy.contains('Spain • 2024 • Regular Season - 20').should('be.visible');

    cy.contains('Arsenal').should('be.visible');
    cy.contains('Manchester City').should('be.visible');
    cy.contains('Barcelona').should('be.visible');
    cy.contains('Real Madrid').should('be.visible');
  });

  it('I should be able to see a login button', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 200,
      body: mockFixturesResponse
    }).as('getFixtures');

    cy.visit('/');
    cy.wait('@getFixtures');
    cy.wait(2000);

    cy.findByRole('button', {name: 'Open Profile Settings'}).click();

    cy.findByRole('button', {name: 'Login'}).as('loginButton')
    cy.get('@loginButton').should('be.visible');
  });

  it('should handle API errors gracefully', () => {
    cy.intercept('GET', '/api/fixtures/upcoming', {
      statusCode: 500,
      body: {error: 'Internal server error'}
    }).as('getFixturesError');

    cy.visit("/")
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

    cy.visit("/")
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

    for (let i = 0; i < 4; i++) {
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
