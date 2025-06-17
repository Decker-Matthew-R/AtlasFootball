import React from 'react';
import ReactDOM from 'react-dom/client';
import { CssBaseline, ThemeProvider } from '@mui/material';
import darkTheme from './themes/darkTheme';
import { Navbar } from './Navbar/Navbar';
import { BrowserRouter } from 'react-router-dom';
import ApplicationRoutes from './Routes/ApplicationRoutes';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider theme={darkTheme}>
      <CssBaseline enableColorScheme />
      <BrowserRouter>
        <Navbar />
        <ApplicationRoutes />
      </BrowserRouter>
    </ThemeProvider>
  </React.StrictMode>,
);
