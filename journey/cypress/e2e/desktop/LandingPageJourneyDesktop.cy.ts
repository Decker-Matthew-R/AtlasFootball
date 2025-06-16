export {}

describe("As a desktop user on the landing page", () => {
  beforeEach(() => {
    cy.viewport(1920, 1080);
    cy.visit('/');
  })
  afterEach(() => {
    Cypress.session.clearAllSavedSessions();
  })

  it('I should see a mobile desktop screen with the correct dimensions of 1920 x 1080', () => {
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
    const siteNameOnNavBar = cy.get('[aria-label="atlas-site-name"]');
    siteNameOnNavBar.should('be.visible').and('contain.text', 'ATLAS');

    const siteLogoDesktop = cy.get('[aria-label="atlas-logo"]');
    siteLogoDesktop.should('be.visible');


    const newsButton = cy.findByRole('button', {name: 'News'});
    newsButton.should('be.visible');

    const matchesButton = cy.findByRole('button', {name: 'Matches'});
    matchesButton.should('be.visible');

    const profileButton = cy.findByRole('button', { name: 'Open Profile Settings' });
    profileButton.should('be.visible');
  });

  it('I should be able to click the profile icon and see profile settings', () => {
    const profileButton = cy.findByRole('button', { name: 'Open Profile Settings' });

    profileButton.click();

    cy.findAllByRole('menuitem').as('profileMenuItems');
    cy.get('@profileMenuItems').should('be.visible');
    cy.get('@profileMenuItems').eq(0).should('be.visible').should('contain.text', 'Profile');
    cy.get('@profileMenuItems').eq(1).should('be.visible').should('contain.text', 'Logout');

    cy.get('body').click();

    cy.get('@profileMenuItems').should('not.exist')
  });

});
