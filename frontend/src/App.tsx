import backgroundImage from './assets/landingPageGenericBackground.jpg';
import { Box } from '@mui/material';

function App() {
  return (
    <Box
      sx={{
        backgroundImage: `url(${backgroundImage})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        width: '100vw',
        height: '100vh',
      }}
      data-testid='landing-page-container'
    ></Box>
  );
}

export default App;
