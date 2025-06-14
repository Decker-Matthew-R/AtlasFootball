import { render, screen } from '@testing-library/react';
import { describe, it } from 'vitest';
import App from '../App';

describe('Landing Page', () => {
  const renderApp = () => render(<App />);

  it('Should contain a background image', async () => {
    renderApp();

    const backgroundBox = screen.getByTestId('landing-page-container');

    expect(backgroundBox).toHaveStyle({
      'background-image': 'url(/src/assets/landingPageGenericBackground.jpg)',
      'background-size': 'cover',
      'background-position': 'center',
      'background-repeat': 'no-repeat',
    });
  });
});
