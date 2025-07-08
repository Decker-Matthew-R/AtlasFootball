import { Route, Routes } from 'react-router-dom';

import LandingPage from '@/LandingPage/LandingPage';
import ProfilePage from '@/ProfilePage/ProfilePage';

const ApplicationRoutes = () => {
  return (
    <Routes>
      <Route
        element={<LandingPage />}
        path='/'
      />
      <Route
        element={<ProfilePage />}
        path='/profile'
      />
    </Routes>
  );
};
export default ApplicationRoutes;
