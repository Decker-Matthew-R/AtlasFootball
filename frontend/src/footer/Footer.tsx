import FacebookIcon from '@mui/icons-material/Facebook';
import InstagramIcon from '@mui/icons-material/Instagram';
import XIcon from '@mui/icons-material/X';
import YouTubeIcon from '@mui/icons-material/YouTube';
import Box from '@mui/material/Box';
import { useTheme } from '@mui/material/styles';

export const Footer = () => {
  const theme = useTheme();

  return (
    <Box
      display='flex'
      flexDirection='row'
      justifyContent='flex-end'
      alignItems='center'
      gap='1rem'
      paddingRight='1rem'
      sx={{
        height: { xs: '56px', sm: '64px', md: '76.5px' },
        backgroundImage: 'linear-gradient(rgba(255, 255, 255, 0.09), rgba(255, 255, 255, 0.09))',
      }}
    >
      <FacebookIcon
        aria-label='facebook-link'
        sx={{
          '&:hover': {
            color: theme.palette.primary.main,
            cursor: 'pointer',
          },
          fontSize: { xs: '20px', sm: '24px', md: '35px' },
        }}
      />
      <InstagramIcon
        aria-label='instagram-link'
        sx={{
          '&:hover': {
            color: theme.palette.primary.main,
            cursor: 'pointer',
          },
          fontSize: { xs: '20px', sm: '24px', md: '35px' },
        }}
      />
      <YouTubeIcon
        aria-label='youtube-link'
        sx={{
          '&:hover': {
            color: theme.palette.primary.main,
            cursor: 'pointer',
          },
          fontSize: { xs: '20px', sm: '24px', md: '35px' },
        }}
      />
      <XIcon
        aria-label='x-link'
        sx={{
          '&:hover': {
            color: theme.palette.primary.main,
            cursor: 'pointer',
          },
          fontSize: { xs: '20px', sm: '24px', md: '35px' },
        }}
      />
    </Box>
  );
};
