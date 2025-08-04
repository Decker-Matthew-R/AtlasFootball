import React from 'react';

import { Card, CardContent, Typography, Box, Chip } from '@mui/material';

import { FixtureDto } from '@/FixturesByLeague/types/FixtureTypes';

interface FixtureCardProps {
  fixture: FixtureDto;
}

const FixtureCard: React.FC<FixtureCardProps> = ({ fixture }) => {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return {
      date: date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
      }),
      time: date.toLocaleTimeString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true,
      }),
    };
  };

  const { date, time } = formatDate(fixture.fixture.date);
  const isLive = fixture.fixture.status.short === 'LIVE';
  const isFinished = fixture.fixture.status.short === 'FT';

  return (
    <Card
      sx={{
        minWidth: 280,
        height: 280,
        display: 'flex',
        flexDirection: 'column',
        flexShrink: 0,
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: 4,
        },
      }}
    >
      <CardContent sx={{ flexGrow: 1, p: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Typography
            variant='caption'
            color='text.secondary'
          >
            {date} • {time}
          </Typography>
          <Chip
            label={fixture.fixture.status.short}
            size='small'
            color={isLive ? 'error' : isFinished ? 'success' : 'default'}
            variant={isLive ? 'filled' : 'outlined'}
          />
        </Box>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box
              component='img'
              src={fixture.teams.home.logo}
              alt={fixture.teams.home.name}
              sx={{
                width: '32px',
                height: '32px',
                objectFit: 'contain',
              }}
            />
            <Typography
              variant='body2'
              sx={{
                flexGrow: 1,
                fontWeight: fixture.teams.home.winner ? 'bold' : 'normal',
              }}
            >
              {fixture.teams.home.name}
            </Typography>
            {fixture.goals.home !== null && (
              <Typography
                variant='h6'
                sx={{
                  fontWeight: 'bold',
                  minWidth: 24,
                  textAlign: 'center',
                }}
              >
                {fixture.goals.home}
              </Typography>
            )}
          </Box>
          <Box sx={{ display: 'flex', justifyContent: 'center', my: 0.5 }}>
            <Typography
              variant='body2'
              color='text.secondary'
              sx={{ fontWeight: 'bold' }}
            >
              {fixture.goals.home !== null ? '' : 'VS'}
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box
              component='img'
              src={fixture.teams.away.logo}
              alt={fixture.teams.away.name}
              sx={{
                width: '32px',
                height: '32px',
                objectFit: 'contain',
              }}
            />
            <Typography
              variant='body2'
              sx={{
                flexGrow: 1,
                fontWeight: fixture.teams.away.winner ? 'bold' : 'normal',
              }}
            >
              {fixture.teams.away.name}
            </Typography>
            {fixture.goals.away !== null && (
              <Typography
                variant='h6'
                sx={{
                  fontWeight: 'bold',
                  minWidth: 24,
                  textAlign: 'center',
                }}
              >
                {fixture.goals.away}
              </Typography>
            )}
          </Box>
        </Box>
        <Box sx={{ mt: 2, pt: 1, borderTop: '1px solid', borderColor: 'divider' }}>
          <Typography
            variant='caption'
            color='text.secondary'
          >
            {fixture.fixture.venue.name}, {fixture.fixture.venue.city}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
};

export default FixtureCard;
