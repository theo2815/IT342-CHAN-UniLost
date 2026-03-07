import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login/Login';
import Register from './pages/Register/Register';
import Dashboard from './pages/Dashboard/Dashboard';
import Profile from './pages/Profile/Profile';
import Settings from './pages/Settings/Settings';
import ItemFeed from './pages/ItemFeed/ItemFeed';
import ItemDetail from './pages/ItemDetail/ItemDetail';
import PostItem from './pages/PostItem/PostItem';
import IncomingClaims from './pages/IncomingClaims/IncomingClaims';
import ClaimDetail from './pages/ClaimDetail/ClaimDetail';
import Notifications from './pages/Notifications/Notifications';
import AdminDashboard from './pages/Admin/AdminDashboard';
import AdminItems from './pages/Admin/AdminItems';
import AdminUsers from './pages/Admin/AdminUsers';
import AdminClaims from './pages/Admin/AdminClaims';
import SuperAdminPanel from './pages/Admin/SuperAdminPanel';
import ProtectedRoute from './components/ProtectedRoute';
import { AdminRoute, FacultyRoute } from './components/AdminRoute';
import Landing from './pages/Landing/Landing';
import ForgotPassword from './pages/ForgotPassword/ForgotPassword';
import VerifyOTP from './pages/VerifyOTP/VerifyOTP';
import ResetPassword from './pages/ResetPassword/ResetPassword';
import MapView from './pages/MapView/MapView';
import Leaderboard from './pages/Leaderboard/Leaderboard';
import Messages from './pages/Messages/Messages';
import { ToastProvider } from './components/Toast';
import './App.css';

import { ThemeProvider } from './context/ThemeContext';

function App() {
  return (
    <ThemeProvider>
      <ToastProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/verify-otp" element={<VerifyOTP />} />
          <Route path="/reset-password" element={<ResetPassword />} />

          {/* Protected Routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/items" element={<ItemFeed />} />
              <Route path="/map" element={<MapView />} />
              <Route path="/leaderboard" element={<Leaderboard />} />
              <Route path="/messages" element={<Messages />} />
              <Route path="/items/:id" element={<ItemDetail />} />
            <Route path="/post-item" element={<PostItem />} />
            <Route path="/my-items/:itemId/claims" element={<IncomingClaims />} />
            <Route path="/claims/:claimId" element={<ClaimDetail />} />
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/settings" element={<Settings />} />

            {/* Admin Routes */}
            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<AdminDashboard />} />
              <Route path="/admin/items" element={<AdminItems />} />
              <Route path="/admin/users" element={<AdminUsers />} />
              <Route path="/admin/claims" element={<AdminClaims />} />
            </Route>
            <Route element={<FacultyRoute />}>
              <Route path="/superadmin" element={<SuperAdminPanel />} />
            </Route>
          </Route>
        </Routes>
      </Router>
      </ToastProvider>
    </ThemeProvider>
  );
}

export default App;
