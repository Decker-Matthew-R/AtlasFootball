import { useEffect, useState } from 'react';

import { Container, Typography } from '@mui/material';
import Box from '@mui/material/Box';

import backgroundImage from '@/assets/landingPageGenericBackground.avif';
import { useFixtures } from '@/FixturesByLeague/client/FixturesClient';
import { groupFixturesByLeague } from '@/FixturesByLeague/helper/fixtureHelpers';
import LeagueCarousel from '@/FixturesByLeague/LeagueCarousel';
import { FixtureDto } from '@/FixturesByLeague/types/FixtureTypes';

function LandingPage() {
  const { fetchFixtures } = useFixtures();
  const [fixtures, setFixtures] = useState<FixtureDto[]>([]);

  useEffect(() => {
    const loadFixtures = async () => {
      try {
        const data = await fetchFixtures();
        setFixtures(data);
      } catch (err) {
        console.error('Failed to load fixtures:', err);
      }
    };

    loadFixtures();
  }, []);

  const fixturesByLeague = groupFixturesByLeague(fixtures);
  const leagueIds = Object.keys(fixturesByLeague).map(Number);

  return (
    <Box
      sx={{
        backgroundImage: `url(${backgroundImage})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        width: '100vw',
        height: { xs: 'calc(100vh - 112px)', sm: 'calc(100vh - 128px)', md: 'calc(100vh - 153px)' },
        overflow: 'auto',
      }}
      data-testid='landing-page-container'
    >
      <Container
        maxWidth='xl'
        sx={{ py: 4 }}
      >
        <Box
          sx={{
            borderRadius: 2,
            backgroundColor: 'rgba(255, 255, 255, 0.05)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            p: 3,
          }}
        >
          <Box>
            {leagueIds.map((leagueId) => (
              <LeagueCarousel
                key={leagueId}
                leagueId={leagueId}
                leagueData={fixturesByLeague[leagueId]}
              />
            ))}

            {leagueIds.length === 0 && (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <Typography
                  variant='body1'
                  color='text.secondary'
                >
                  No fixtures available at the moment.
                </Typography>
              </Box>
            )}
          </Box>
        </Box>
      </Container>
    </Box>
  );
}

export default LandingPage;
