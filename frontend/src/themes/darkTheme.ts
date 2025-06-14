import { createTheme } from '@mui/material/styles';

// Your brand colors
const brandColors = {
  primary: '#2D5CAE',
  black: '#000000',
  white: '#FFFFFF',
};

// Generate primary color variants
const primaryVariants = {
  50: '#E8F0FF',
  100: '#C6DAFF',
  200: '#A1C4FF',
  300: '#7CAEFF',
  400: '#5D98FF',
  500: brandColors.primary, // #2D5CAE - Main brand color
  600: '#2650A3',
  700: '#1E4297',
  800: '#17358C',
  900: '#0D2580',
};

// Create the dark theme
const darkTheme = createTheme({
  palette: {
    mode: 'dark',

    // Primary color (your brand blue)
    primary: {
      main: brandColors.primary,     // #2D5CAE
      light: primaryVariants[400],   // Lighter blue for hover states
      dark: primaryVariants[700],    // Darker blue for pressed states
      contrastText: brandColors.white,
    },

    // Secondary color (using a complementary approach)
    secondary: {
      main: '#404040',               // Dark grey for secondary actions
      light: '#606060',              // Lighter grey
      dark: '#202020',               // Darker grey
      contrastText: brandColors.white,
    },

    // Background colors - black-based
    background: {
      default: brandColors.black,    // #000000 - Main page background
      paper: '#0A0A0A',             // Slightly lighter for cards/modals
    },

    // Surface colors for elevated components
    surface: {
      main: '#151515',              // For elevated surfaces
      light: '#202020',             // For hover states
      dark: '#0A0A0A',              // For pressed states
    },

    // Text colors optimized for dark theme
    text: {
      primary: brandColors.white,    // #FFFFFF - Primary text
      secondary: '#B0B0B0',         // Light grey for secondary text
      disabled: '#606060',          // Darker grey for disabled text
    },

    // Semantic colors adapted for dark theme
    error: {
      main: '#FF5252',
      light: '#FF7A7A',
      dark: '#D32F2F',
      contrastText: brandColors.white,
    },

    warning: {
      main: '#FFA726',
      light: '#FFB74D',
      dark: '#F57C00',
      contrastText: brandColors.black,
    },

    info: {
      main: '#29B6F6',
      light: '#4FC3F7',
      dark: '#0288D1',
      contrastText: brandColors.white,
    },

    success: {
      main: '#66BB6A',
      light: '#81C784',
      dark: '#388E3C',
      contrastText: brandColors.white,
    },

    // Grey scale for dark theme
    grey: {
      50: '#FAFAFA',
      100: '#F5F5F5',
      200: '#EEEEEE',
      300: '#E0E0E0',
      400: '#BDBDBD',
      500: '#9E9E9E',
      600: '#757575',
      700: '#616161',
      800: '#424242',
      900: '#212121',
    },

    // Divider and border colors
    divider: '#303030',

    // Action colors for interactive elements
    action: {
      active: brandColors.white,
      hover: 'rgba(255, 255, 255, 0.08)',
      selected: 'rgba(45, 92, 174, 0.12)',
      disabled: '#606060',
      disabledBackground: '#1A1A1A',
      focus: 'rgba(255, 255, 255, 0.12)',
    },
  },

  // Typography optimized for dark theme
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 600,
      lineHeight: 1.2,
      color: brandColors.white,
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 600,
      lineHeight: 1.3,
      color: brandColors.white,
    },
    h3: {
      fontSize: '1.75rem',
      fontWeight: 500,
      lineHeight: 1.4,
      color: brandColors.white,
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 500,
      lineHeight: 1.4,
      color: brandColors.white,
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 500,
      lineHeight: 1.5,
      color: brandColors.white,
    },
    h6: {
      fontSize: '1rem',
      fontWeight: 600,
      lineHeight: 1.6,
      color: brandColors.white,
    },
    body1: {
      fontSize: '1rem',
      fontWeight: 400,
      lineHeight: 1.5,
      color: brandColors.white,
    },
    body2: {
      fontSize: '0.875rem',
      fontWeight: 400,
      lineHeight: 1.43,
      color: '#B0B0B0',
    },
    button: {
      fontSize: '0.875rem',
      fontWeight: 500,
      lineHeight: 1.75,
      textTransform: 'none', // Remove uppercase
    },
  },

  // Spacing and shape
  spacing: 8,
  shape: {
    borderRadius: 8,
  },

  // Component customizations for dark theme
  components: {
    // Global CSS baseline
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: brandColors.black,
          color: brandColors.white,
        },
        // Remove default focus outlines and replace with custom ones
        '*:focus': {
          outline: 'none',
        },
        // Custom focus styles for accessibility
        'button:focus-visible, input:focus-visible, textarea:focus-visible, select:focus-visible': {
          outline: `2px solid ${brandColors.white}`,
          outlineOffset: '2px',
        },
      },
    },

    // Button customizations
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 500,
          padding: '10px 20px',
          boxShadow: 'none',
          // Remove default focus outline
          '&:focus': {
            outline: 'none',
          },
          // Custom focus styles
          '&:focus-visible': {
            outline: `2px solid ${brandColors.white}`,
            outlineOffset: '2px',
          },
          '&:hover': {
            boxShadow: '0px 4px 12px rgba(45, 92, 174, 0.25)',
          },
        },
        contained: {
          '&:hover': {
            boxShadow: '0px 6px 16px rgba(45, 92, 174, 0.3)',
          },
          '&:focus-visible': {
            outline: `2px solid ${brandColors.white}`,
            outlineOffset: '2px',
            boxShadow: '0px 6px 16px rgba(45, 92, 174, 0.3)',
          },
        },
        outlined: {
          borderWidth: '1.5px',
          borderColor: '#303030',
          '&:hover': {
            borderWidth: '1.5px',
            borderColor: brandColors.primary,
            backgroundColor: 'rgba(45, 92, 174, 0.08)',
          },
          '&:focus-visible': {
            outline: `2px solid ${brandColors.white}`,
            outlineOffset: '2px',
            borderColor: brandColors.primary,
          },
        },
        text: {
          '&:focus-visible': {
            outline: `2px solid ${brandColors.white}`,
            outlineOffset: '2px',
            backgroundColor: 'rgba(255, 255, 255, 0.08)',
          },
        },
      },
    },

    // Card customizations
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundColor: '#0A0A0A',
          borderRadius: 12,
          border: '1px solid #1A1A1A',
          boxShadow: '0px 4px 12px rgba(0, 0, 0, 0.3)',
          '&:hover': {
            boxShadow: '0px 6px 20px rgba(0, 0, 0, 0.4)',
            borderColor: '#303030',
          },
        },
      },
    },

    // TextField customizations
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            backgroundColor: '#0A0A0A',
            borderRadius: 8,
            '& fieldset': {
              borderColor: '#303030',
            },
            '&:hover fieldset': {
              borderColor: '#505050',
            },
            '&.Mui-focused fieldset': {
              borderColor: brandColors.primary,
            },
            // Remove the white outline styles here
          },
          '& .MuiInputLabel-root': {
            color: '#B0B0B0',
          },
          '& .MuiInputBase-input': {
            color: brandColors.white,
            '&:focus': {
              outline: 'none',
            },
          },
        },
      },
    },

    // Paper customizations
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundColor: '#0A0A0A',
          borderRadius: 8,
        },
        elevation1: {
          boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.2)',
        },
        elevation2: {
          boxShadow: '0px 4px 8px rgba(0, 0, 0, 0.25)',
        },
        elevation3: {
          boxShadow: '0px 6px 12px rgba(0, 0, 0, 0.3)',
        },
      },
    },

    // AppBar customizations
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: brandColors.black,
          borderBottom: '1px solid #1A1A1A',
          boxShadow: '0px 1px 3px rgba(0, 0, 0, 0.3)',
        },
      },
    },

    // Chip customizations
    MuiChip: {
      styleOverrides: {
        root: {
          backgroundColor: '#1A1A1A',
          color: brandColors.white,
          borderRadius: 16,
          '&:hover': {
            backgroundColor: '#303030',
          },
        },
        outlined: {
          borderColor: '#303030',
          '&:hover': {
            borderColor: '#505050',
          },
        },
      },
    },

    // Dialog customizations
    MuiDialog: {
      styleOverrides: {
        paper: {
          backgroundColor: '#0A0A0A',
          borderRadius: 12,
          border: '1px solid #1A1A1A',
        },
      },
    },

    // List customizations
    MuiListItem: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.05)',
          },
          '&.Mui-selected': {
            backgroundColor: 'rgba(45, 92, 174, 0.12)',
            '&:hover': {
              backgroundColor: 'rgba(45, 92, 174, 0.16)',
            },
          },
        },
      },
    },

    // Tabs customizations
    MuiTabs: {
      styleOverrides: {
        indicator: {
          backgroundColor: brandColors.primary,
        },
      },
    },

    // Switch customizations
    MuiSwitch: {
      styleOverrides: {
        switchBase: {
          '&.Mui-checked': {
            color: brandColors.primary,
            '& + .MuiSwitch-track': {
              backgroundColor: brandColors.primary,
            },
          },
        },
        track: {
          backgroundColor: '#505050',
        },
      },
    },
  },

  // Custom breakpoints
  breakpoints: {
    values: {
      xs: 0,
      sm: 600,
      md: 900,
      lg: 1200,
      xl: 1536,
    },
  },
});

export default darkTheme;
