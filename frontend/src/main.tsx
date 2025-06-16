import React from 'react';
import ReactDOM from 'react-dom/client';
// @ts-expect-error from App.tsx
import App from './App.tsx';
import { CssBaseline, ThemeProvider } from '@mui/material';
import darkTheme from './themes/darkTheme';
import Navbar from './Navbar/Navbar';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider theme={darkTheme}>
      <CssBaseline enableColorScheme />
      <Navbar />
      <App />
    </ThemeProvider>
  </React.StrictMode>,
);
