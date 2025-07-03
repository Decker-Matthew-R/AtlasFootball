import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import { useLocation } from 'react-router-dom';

import backgroundImage from '@/assets/landingPageGenericBackground.jpg';
import { useUser } from '@/GlobalContext/UserContext/UserContext';
import { useMetrics } from '@/metrics/client/MetricsClient';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';

function LandingPage() {
  const { user } = useUser();
  const { saveMetricEvent } = useMetrics();
  const currentLocation = useLocation();

  const handleLogin = () => {
    saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, {
      triggerId: 'Login',
      screen: currentLocation.pathname,
    });
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
      {user ? (
        <></>
      ) : (
        <Button
          aria-label='login-button'
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
      )}
    </Box>
  );
}

export default LandingPage;
