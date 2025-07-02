import { Box } from '@mui/material';
import Button from '@mui/material/Button';

import backgroundImage from '@/assets/landingPageGenericBackground.jpg';

function LandingPage() {
  const handleLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

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
    >
      <Button
        variant='contained'
        onClick={handleLogin}
        sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
        }}
      >
        Login
      </Button>
    </Box>
  );
}

export default LandingPage;
