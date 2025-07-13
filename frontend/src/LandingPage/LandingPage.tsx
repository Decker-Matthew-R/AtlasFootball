import { useEffect, useState } from 'react';

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import { useLocation } from 'react-router-dom';

import backgroundImage from '@/assets/landingPageGenericBackground.avif';
import { useUser } from '@/GlobalContext/UserContext/UserContext';
import { useFixtures } from '@/LandingPage/client/FixturesClient';
import { useMetrics } from '@/metrics/client/MetricsClient';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';

import { FixtureDto, FixtureResponseDto } from './model/FixtureTypes';

function LandingPage() {
  const { user } = useUser();
  const { saveMetricEvent } = useMetrics();
  const [fixtures, setFixtures] = useState<FixtureDto[]>([]);
  const { getUpcomingFixtures } = useFixtures();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const currentLocation = useLocation();

  const handleLogin = () => {
    saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, {
      triggerId: 'Login',
      screen: currentLocation.pathname,
    });
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  useEffect(() => {
    const fetchFixtures = async () => {
      if (!user) return;

      try {
        setLoading(true);
        setError(null);

        const response: FixtureResponseDto = await getUpcomingFixtures();

        if (response.status === 'success') {
          setFixtures(response.fixtures);
        } else {
          setError(response.message);
        }
      } catch (err) {
        setError('Failed to load upcoming fixtures');
        console.error('Error fetching fixtures:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchFixtures();
  }, [user]);

  return (
    <Box
      sx={{
        backgroundImage: `url(${backgroundImage})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        width: '100vw',
        height: { xs: 'calc(100vh - 112px)', sm: 'calc(100vh - 128px)', md: 'calc(100vh - 153px)' },
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
