import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it } from 'vitest';

import { Footer } from '@/footer/Footer';

describe('Footer', () => {
  const renderFooter = () => {
    render(
      <BrowserRouter>
        <Footer />
      </BrowserRouter>,
    );
  };
  it('should render footer with social icons', () => {
    renderFooter();

    const facebookIcon = screen.getByLabelText('facebook-link');
    expect(facebookIcon).toBeVisible();

    const instagramIcon = screen.getByLabelText('instagram-link');
    expect(instagramIcon).toBeVisible();

    const youtubeIcon = screen.getByLabelText('youtube-link');
    expect(youtubeIcon).toBeVisible();

    const xIcon = screen.getByLabelText('x-link');
    expect(xIcon).toBeVisible();
  });
});
