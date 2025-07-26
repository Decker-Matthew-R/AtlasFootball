import React, { useRef, useState, useEffect } from 'react';

import { ChevronLeft, ChevronRight } from '@mui/icons-material';
import { Box, Typography, IconButton } from '@mui/material';

import { FixturesByLeague } from '@/FixturesByLeague/types/FixtureTypes';

import FixtureCard from './FixtureCard';

interface LeagueCarouselProps {
  leagueId: number;
  leagueData: FixturesByLeague[number];
}

const LeagueCarousel: React.FC<LeagueCarouselProps> = ({ leagueData }) => {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const scrollAmount = 300;

  const updateScrollButtons = () => {
    if (!scrollContainerRef.current) return;

    const { scrollLeft, scrollWidth, clientWidth } = scrollContainerRef.current;
    setCanScrollLeft(scrollLeft > 0);
    setCanScrollRight(scrollLeft < scrollWidth - clientWidth - 5);
  };

  const scrollLeft = () => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollBy({
        left: -scrollAmount,
        behavior: 'smooth',
      });
    }
  };

  const scrollRight = () => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollBy({
        left: scrollAmount,
        behavior: 'smooth',
      });
    }
  };

  useEffect(() => {
    const scrollContainer = scrollContainerRef.current;
    if (scrollContainer) {
      scrollContainer.addEventListener('scroll', updateScrollButtons);
      updateScrollButtons();

      return () => {
        scrollContainer.removeEventListener('scroll', updateScrollButtons);
      };
    }
  }, []);

  return (
    <Box sx={{ mb: 4 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
        <Box
          sx={{
            width: 48,
            height: 48,
            backgroundColor: 'white',
            border: '1px solid rgba(0, 0, 0, 0.1)',
            borderRadius: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            overflow: 'hidden',
          }}
        >
          <Box
            component='img'
            src={leagueData.league.logo}
            alt={leagueData.league.name}
            sx={{
              maxWidth: '90%',
              maxHeight: '90%',
              objectFit: 'cover',
            }}
          />
        </Box>
        <Box>
          <Typography
            variant='h5'
            component='h2'
            sx={{ fontWeight: 'bold' }}
          >
            {leagueData.league.name}
          </Typography>
          <Typography
            variant='body2'
            color='text.secondary'
          >
            {leagueData.league.country} • {leagueData.league.season} • {leagueData.league.round}
          </Typography>
        </Box>
      </Box>
      <Box sx={{ position: 'relative' }}>
        <IconButton
          onClick={scrollLeft}
          disabled={!canScrollLeft}
          aria-label='Scroll left'
          sx={{
            position: 'absolute',
            left: -20,
            top: '50%',
            transform: 'translateY(-50%)',
            zIndex: 10,
            width: 40,
            height: 40,
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            color: canScrollRight ? 'secondary.main' : 'action.disabled',
            border: '1px solid',
            borderColor: 'divider',
            boxShadow: 2,
            transition: 'all 0.2s',
            '&:hover': {
              backgroundColor: 'white',
              boxShadow: 4,
              transform: 'translateY(-50%) scale(1.05)',
            },
            '&:disabled': {
              opacity: 0.3,
              cursor: 'not-allowed',
            },
          }}
        >
          <ChevronLeft />
        </IconButton>
        <Box
          ref={scrollContainerRef}
          sx={{
            display: 'flex',
            gap: 2,
            overflowX: 'auto',
            scrollBehavior: 'smooth',
            pb: 1,
            msOverflowStyle: 'none',
            scrollbarWidth: 'none',
            '&::-webkit-scrollbar': {
              display: 'none',
            },
          }}
        >
          {leagueData.fixtures.map((fixture) => (
            <FixtureCard
              key={fixture.fixture.id}
              fixture={fixture}
            />
          ))}
        </Box>
        <IconButton
          onClick={scrollRight}
          disabled={!canScrollRight}
          aria-label='Scroll right'
          sx={{
            position: 'absolute',
            right: -20,
            top: '50%',
            transform: 'translateY(-50%)',
            zIndex: 10,
            width: 40,
            height: 40,
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            color: canScrollRight ? 'secondary.main' : 'action.disabled',
            border: '1px solid',
            borderColor: 'divider',
            boxShadow: 2,
            transition: 'all 0.2s',
            '&:hover': {
              backgroundColor: 'white',
              boxShadow: 4,
              transform: 'translateY(-50%) scale(1.05)',
            },
            '&:disabled': {
              opacity: 0.3,
              cursor: 'not-allowed',
            },
          }}
        >
          <ChevronRight />
        </IconButton>
      </Box>
    </Box>
  );
};

export default LeagueCarousel;
