import { render, screen } from '@testing-library/react';
import { describe, it } from 'vitest';

import LandingPage from '../LandingPage';

describe('Landing Page', () => {
  const renderApp = () => render(<LandingPage />);

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
