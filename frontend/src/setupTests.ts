import '@testing-library/jest-dom';
import matchers from '@testing-library/jest-dom/matchers';
import { afterAll, afterEach, beforeAll, expect } from 'vitest';
import { config } from 'react-transition-group';
import { setupServer } from 'msw/node';

// Removes MUI animations for testing
config.disabled = true;

expect.extend(matchers);

export const server = setupServer();
beforeAll(() => {
  server.listen();
});

afterEach(() => {
  server.resetHandlers();
});

afterAll(() => {
  server.close();
});
