export {}


declare global {
  namespace Cypress {
    interface Chainable {
      loginViaBackend(options?: { email?: string; name?: string }): Chainable<void>
    }
  }
}

Cypress.Commands.add('loginViaBackend', (options = {}) => {
  const { email = 'test-user@example.com', name = 'Test User' } = options

  cy.log('🔐 Logging in via backend test endpoint...')

  cy.request({
    method: 'POST',
    url: '/api/test/login',
    body: {
      email: email,
      name: name
    },
    failOnStatusCode: false
  }).then((response) => {
    cy.log('Backend response status:', response.status)
    cy.log('Backend response body:', response.body)

    if (response.status === 200) {
      cy.log('✅ Backend login successful')
      cy.log('User:', response.body.user?.email || 'Unknown')
    } else {
      cy.log('❌ Backend login failed:', response.body)
      throw new Error(`Backend login failed: ${response.body?.error || response.statusText}`)
    }
  })
})
