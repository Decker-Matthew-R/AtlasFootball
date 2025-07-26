import { useEffect, useState } from 'react';

import { Typography } from '@mui/material';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import { useLocation } from 'react-router-dom';

import backgroundImage from '@/assets/landingPageGenericBackground.avif';
import { useFixtures } from '@/FixturesByLeague/client/FixturesClient';
import { groupFixturesByLeague } from '@/FixturesByLeague/helper/fixtureHelpers';
import { FixtureDto } from '@/FixturesByLeague/types/FixtureTypes';
import { useUser } from '@/GlobalContext/UserContext/UserContext';
import { useMetrics } from '@/Metrics/client/MetricsClient';
import { METRIC_EVENT_TYPE } from '@/Metrics/model/METRIC_EVENT_TYPE';

function LandingPage() {
  const [fixtures, setFixtures] = useState<FixtureDto[]>([]);

  const { user } = useUser();
  const { saveMetricEvent } = useMetrics();
  const { fetchFixtures } = useFixtures();
  const currentLocation = useLocation();

  useEffect(() => {
    const loadFixtures = async () => {
      try {
        const data = await fetchFixtures();
        setFixtures(data);
        console.log('Raw fixtures data:', data);
        console.log('Grouped by league:', groupFixturesByLeague(data));
      } catch (err) {
        console.error('Failed to load fixtures:', err);
      }
    };

    loadFixtures();
  });

  const handleLogin = () => {
    saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, {
      triggerId: 'Login',
      screen: currentLocation.pathname,
    });
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  const fixturesByLeague = groupFixturesByLeague(fixtures);

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
      <Box>
        <Typography variant='h6'>Fixtures Data Test</Typography>
        <Typography>Total fixtures: {fixtures.length}</Typography>
        <Typography>Leagues found: {Object.keys(fixturesByLeague).length}</Typography>

        {Object.entries(fixturesByLeague).map(([leagueId, leagueData]) => (
          <Box
            key={leagueId}
            sx={{ mt: 2, p: 1, border: '1px solid #ddd' }}
          >
            <Typography variant='subtitle1'>{leagueData.league.name}</Typography>
            <Typography variant='body2'>Fixtures: {leagueData.fixtures.length}</Typography>

            {leagueData.fixtures.slice(0, 2).map((fixture) => (
              <Typography
                key={fixture.fixture.id}
                variant='body2'
                sx={{ ml: 1 }}
              >
                • {fixture.teams.home.name} vs {fixture.teams.away.name}
              </Typography>
            ))}
          </Box>
        ))}
      </Box>
    </Box>
  );
}

export default LandingPage;
