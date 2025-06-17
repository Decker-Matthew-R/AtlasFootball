import React from 'react';
import ReactDOM from 'react-dom/client';
import LandingPage from './LandingPage/LandingPage';
import { CssBaseline, ThemeProvider } from '@mui/material';
import darkTheme from './themes/darkTheme';
import Navbar from './Navbar/Navbar';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider theme={darkTheme}>
      <CssBaseline enableColorScheme />
      <Navbar />
      <LandingPage />
    </ThemeProvider>
  </React.StrictMode>,
);
