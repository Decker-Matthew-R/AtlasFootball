import { Box } from '@mui/material';

import backgroundImage from '@/assets/landingPageGenericBackground.jpg';

function LandingPage() {
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

export default LandingPage;
