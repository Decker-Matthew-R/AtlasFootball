import React from 'react';

import { CssBaseline } from '@mui/material';
import { ThemeProvider } from '@mui/material/styles';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';

import { Footer } from '@/footer/Footer';
import { UserProvider } from '@/GlobalContext/UserContext/UserContext';
import { Navbar } from '@/Navbar/Navbar';
import ApplicationRoutes from '@/Routes/ApplicationRoutes';
import darkTheme from '@/themes/darkTheme';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider theme={darkTheme}>
      <CssBaseline enableColorScheme />
      <UserProvider>
        <BrowserRouter>
          <Navbar />
          <ApplicationRoutes />
          <Footer />
        </BrowserRouter>
      </UserProvider>
    </ThemeProvider>
  </React.StrictMode>,
);
