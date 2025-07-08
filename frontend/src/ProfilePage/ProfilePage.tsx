import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

import { useUser } from '@/GlobalContext/UserContext/UserContext';

const ProfilePage = () => {
  const { user } = useUser();
  return (
    <Box
      display='flex'
      flexDirection='column'
      alignItems='center'
      gap='1rem'
      sx={{
        width: '100vw',
        height: { xs: 'calc(100vh - 112px)', sm: 'calc(100vh - 128px)', md: 'calc(100vh - 153px)' },
      }}
    >
      <Avatar
        aria-label='profile-page-user-avatar'
        alt={user?.name ? user?.name.charAt(0) : ''}
        src={user?.profilePicture || undefined}
        sx={{ height: '168px', width: '168px', marginTop: '2rem' }}
      >
        {!user?.profilePicture && (user?.name ? user?.name.charAt(0) : '')}
      </Avatar>
      <Typography variant='h1'>{user?.name}</Typography>
    </Box>
  );
};

export default ProfilePage;
