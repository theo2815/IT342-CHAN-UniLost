import { Navigate, Outlet, useLocation } from "react-router-dom";
import authService from "../../features/auth/authService";

const ProtectedRoute = () => {
  const isAuthenticated = authService.isAuthenticated();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
