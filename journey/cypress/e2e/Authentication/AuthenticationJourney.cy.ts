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

    cy.visit('/')

    cy.getCookie('jwt').should('exist')
    cy.getCookie('user_info').should('exist')

    cy.log('✅ Backend authentication test passed!')
  })
})
