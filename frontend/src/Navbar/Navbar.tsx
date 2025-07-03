import * as React from 'react';

import AdbIcon from '@mui/icons-material/Adb';
import MenuIcon from '@mui/icons-material/Menu';
import AppBar from '@mui/material/AppBar';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import IconButton from '@mui/material/IconButton';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Toolbar from '@mui/material/Toolbar';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { useLocation, useNavigate } from 'react-router-dom';

import { useUser } from '@/GlobalContext/UserContext/UserContext';
import { saveMetricEvent } from '@/metrics/client/MetricsClient';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';
import { MetricEventType } from '@/metrics/model/MetricEventType';

const pages = [
  { page: 'News', route: '/' },
  { page: 'Matches', route: '/' },
];
const settings = [
  { setting: 'Profile', route: '/' },
  { setting: 'Logout', route: '/' },
];

export const Navbar = () => {
  const [anchorElNav, setAnchorElNav] = React.useState<null | HTMLElement>(null);
  const [anchorElUser, setAnchorElUser] = React.useState<null | HTMLElement>(null);

  const { user } = useUser();

  const navigate = useNavigate();
  const currentLocation = useLocation();

  const handleOpenNavMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElNav(event.currentTarget);
  };
  const handleOpenUserMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElUser(event.currentTarget);
  };

  const handleCloseNavMenu = () => {
    setAnchorElNav(null);
  };

  const handleCloseUserMenu = () => {
    setAnchorElUser(null);
  };

  const handleBrandingClick = (brandingClicked: string) => {
    const metricEvent: MetricEventType = {
      event: METRIC_EVENT_TYPE.BUTTON_CLICK,
      eventMetadata: {
        triggerId: brandingClicked,
        screen: currentLocation.pathname,
      },
    };
    saveMetricEvent(metricEvent);
    navigate('/');
  };

  const handleMenuItemNavigation = (route: string, buttonClicked: string) => {
    const metricEvent: MetricEventType = {
      event: METRIC_EVENT_TYPE.BUTTON_CLICK,
      eventMetadata: {
        triggerId: buttonClicked,
        screen: currentLocation.pathname,
      },
    };
    saveMetricEvent(metricEvent);
    navigate(route);
  };

  return (
    <AppBar position='static'>
      <Container maxWidth={false}>
        <Toolbar disableGutters>
          <AdbIcon
            aria-label='atlas-logo'
            onClick={() => handleBrandingClick('Atlas Logo')}
            sx={{ display: { xs: 'none', md: 'flex' }, mr: 1, cursor: 'pointer' }}
          />
          <Typography
            aria-label={'atlas-site-name'}
            onClick={() => handleBrandingClick('Atlas Name')}
            variant='h6'
            noWrap
            component='a'
            sx={{
              mr: 2,
              display: { xs: 'none', md: 'flex' },
              fontFamily: 'monospace',
              fontWeight: 700,
              letterSpacing: '.3rem',
              color: 'inherit',
              textDecoration: 'none',
              cursor: 'pointer',
            }}
          >
            ATLAS
          </Typography>

          <Box sx={{ flexGrow: 1, display: { xs: 'flex', md: 'none' } }}>
            <IconButton
              size='large'
              aria-label='navigation-links'
              aria-controls='menu-appbar'
              aria-haspopup='true'
              onClick={handleOpenNavMenu}
              color='inherit'
            >
              <MenuIcon />
            </IconButton>
            <Menu
              id='menu-appbar'
              anchorEl={anchorElNav}
              anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'left',
              }}
              keepMounted
              transformOrigin={{
                vertical: 'top',
                horizontal: 'left',
              }}
              open={Boolean(anchorElNav)}
              onClose={handleCloseNavMenu}
              sx={{ display: { xs: 'block', md: 'none' } }}
            >
              {pages.map((page) => (
                <MenuItem
                  key={page.page}
                  onClick={() => {
                    handleCloseNavMenu();
                    handleMenuItemNavigation(page.route, page.page);
                  }}
                >
                  <Typography sx={{ textAlign: 'center' }}>{page.page}</Typography>
                </MenuItem>
              ))}
            </Menu>
          </Box>
          <AdbIcon
            aria-label={'atlas-logo-mobile'}
            onClick={() => handleBrandingClick('Atlas Logo')}
            sx={{ display: { xs: 'flex', md: 'none' }, mr: 1, cursor: 'pointer' }}
          />
          <Typography
            aria-label={'atlas-site-name-mobile'}
            onClick={() => handleBrandingClick('Atlas Name')}
            variant='h5'
            noWrap
            component='a'
            sx={{
              mr: 2,
              display: { xs: 'flex', md: 'none' },
              flexGrow: 1,
              fontFamily: 'monospace',
              fontWeight: 700,
              letterSpacing: '.3rem',
              color: 'inherit',
              textDecoration: 'none',
              cursor: 'pointer',
            }}
          >
            ATLAS
          </Typography>
          <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
            {pages.map((page) => (
              <Button
                key={page.page}
                onClick={() => {
                  handleCloseNavMenu();
                  handleMenuItemNavigation(page.route, page.page);
                }}
                sx={{ my: 2, color: 'white', display: 'block' }}
              >
                {page.page}
              </Button>
            ))}
          </Box>
          <Box sx={{ flexGrow: 0 }}>
            <Tooltip title='Open Profile Settings'>
              <IconButton
                onClick={handleOpenUserMenu}
                sx={{ p: 0 }}
              >
                <Avatar
                  alt={user?.name ? user?.name.charAt(0) : ''}
                  src={user?.profilePicture || undefined}
                >
                  {!user?.profilePicture && (user?.name ? user?.name.charAt(0) : '')}
                </Avatar>
              </IconButton>
            </Tooltip>
            <Menu
              sx={{ mt: '45px' }}
              id='menu-appbar'
              anchorEl={anchorElUser}
              anchorOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              keepMounted
              transformOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              open={Boolean(anchorElUser)}
              onClose={handleCloseUserMenu}
            >
              {settings.map((setting) => (
                <MenuItem
                  key={setting.setting}
                  onClick={() => {
                    handleCloseUserMenu();
                    handleMenuItemNavigation(setting.route, setting.setting);
                  }}
                >
                  <Typography sx={{ textAlign: 'center' }}>{setting.setting}</Typography>
                </MenuItem>
              ))}
            </Menu>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
};
