import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import App from '../App';
import { act } from 'react-dom/test-utils';

describe('render app', () => {
  const renderApp = () => render(<App />);

  it('should render app images', async () => {
    // renderApp();
    //
    // const countButton = screen.getByRole('button', { name: 'count is 0' });
    //
    // expect(countButton).toBeVisible();
    //
    // act(() => {
    //   countButton.click();
    // });
    //
    // const onScreenText = screen.getByText('count is 1');
    // expect(onScreenText).toBeVisible();
  });
});
