import { Navigate, Outlet, useLocation } from "react-router-dom";
import authService from "../services/authService";

const ProtectedRoute = () => {
  const isAuthenticated = authService.isAuthenticated();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
