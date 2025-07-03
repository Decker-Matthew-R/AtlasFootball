export {}

describe("As a mobile user on the landing page", () => {
  beforeEach(() => {
    cy.viewport(375, 667);
    cy.visit('/');
  })
  afterEach(() => {
    Cypress.session.clearAllSavedSessions();
  })

  it('I should see a mobile desktop screen with the correct dimensions of 375 x 667', () => {
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

  it('I should be able to see a login button', () => {
    cy.findByLabelText('login-button').as('loginButton')
    cy.get('@loginButton').should('be.visible')

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
});
