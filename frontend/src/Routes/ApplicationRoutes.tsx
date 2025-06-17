import { Route, Routes } from 'react-router-dom';
import LandingPage from '../LandingPage/LandingPage';
const ApplicationRoutes = () => {
  return (
    <Routes>
      <Route
        element={<LandingPage />}
        path='/'
      />
    </Routes>
  );
};
export default ApplicationRoutes;
