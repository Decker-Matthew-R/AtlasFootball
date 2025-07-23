export {}

describe("As a desktop user on the profile page", () => {
  beforeEach(() => {
    cy.viewport(1920, 1080);
    cy.loginViaBackend({
      email: 'authenticated-test@example.com',
      name: 'Authenticated User'
    });

    cy.visit('/');
  })
  afterEach(() => {
    Cypress.session.clearAllSavedSessions();
  })


  it('I should be able to click the profile icon and profile page and see profile information', () => {
    const profileButton = cy.findByRole('button', { name: 'Open Profile Settings' });

    profileButton.click();

    cy.findAllByRole('menuitem').as('profileMenuItems');
    cy.get('@profileMenuItems').eq(0).should('be.visible').should('contain.text', 'Profile').as('profilePage');

    cy.get('@profilePage').click();

    cy.findByText('Authenticated User').as('userName');
    cy.get('@userName').should('be.visible');

    cy.findByLabelText('profile-page-user-avatar').as('avatar');
    cy.get('@avatar').should('be.visible')

    cy.get('@avatar').should('contain.text', 'A')

  });
});
