import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './features/auth/Login/Login';
import Register from './features/auth/Register/Register';
import Dashboard from './features/user/Dashboard/Dashboard';
import Profile from './features/user/Profile/Profile';
import Settings from './features/user/Settings/Settings';
import ItemFeed from './features/items/ItemFeed/ItemFeed';
import ItemDetail from './features/items/ItemDetail/ItemDetail';
import PostItem from './features/items/PostItem/PostItem';
import IncomingClaims from './features/claims/IncomingClaims/IncomingClaims';
import ClaimDetail from './features/claims/ClaimDetail/ClaimDetail';
import Notifications from './features/user/Notifications/Notifications';
import AdminDashboard from './features/admin/AdminDashboard';
import AdminItems from './features/admin/AdminItems';
import AdminUsers from './features/admin/AdminUsers';
import AdminClaims from './features/admin/AdminClaims';
import AdminReports from './features/admin/AdminReports';
import CampusManagement from './features/admin/CampusManagement';
import AdminAuditLogs from './features/admin/AdminAuditLogs';
import AdminAnalytics from './features/admin/AdminAnalytics';
import AdminHealth from './features/admin/AdminHealth';
import ProtectedRoute from './shared/routing/ProtectedRoute';
import { AdminRoute } from './shared/routing/AdminRoute';
import Landing from './features/public/Landing/Landing';
import ForgotPassword from './features/auth/ForgotPassword/ForgotPassword';
import VerifyOTP from './features/auth/VerifyOTP/VerifyOTP';
import ResetPassword from './features/auth/ResetPassword/ResetPassword';
import MapView from './features/items/MapView/MapView';
import Leaderboard from './features/user/Leaderboard/Leaderboard';
import Messages from './features/messaging/Messages/Messages';
import NotFound from './features/notfound/NotFound/NotFound';
import { APIProvider } from '@vis.gl/react-google-maps';
import { ToastProvider } from './shared/components/ui/Toast';
import { useGlobalToast } from './shared/hooks/useToast';
import './App.css';

import { ThemeProvider } from './shared/context/ThemeContext';
import { CampusProvider } from './shared/context/CampusContext';
import { UnreadProvider } from './shared/context/UnreadContext';

const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '';

function GlobalToastInit() {
  useGlobalToast();
  return null;
}

function App() {
  return (
    <APIProvider apiKey={GOOGLE_MAPS_API_KEY}>
    <ThemeProvider>
      <CampusProvider>
      <UnreadProvider>
      <ToastProvider>
      <GlobalToastInit />
      <Router>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/verify-otp" element={<VerifyOTP />} />
          <Route path="/reset-password" element={<ResetPassword />} />

          {/* Public browse routes (guests get read-only access) */}
          <Route path="/items" element={<ItemFeed />} />
          <Route path="/items/:id" element={<ItemDetail />} />
          <Route path="/map" element={<MapView />} />
          <Route path="/leaderboard" element={<Leaderboard />} />

          {/* Protected Routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/messages" element={<Messages />} />
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
              <Route path="/admin/reports" element={<AdminReports />} />
              <Route path="/admin/campuses" element={<CampusManagement />} />
              <Route path="/admin/audit-logs" element={<AdminAuditLogs />} />
              <Route path="/admin/analytics" element={<AdminAnalytics />} />
              <Route path="/admin/health" element={<AdminHealth />} />
            </Route>
          </Route>

          {/* Catch-all 404 — must be last */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Router>
      </ToastProvider>
      </UnreadProvider>
      </CampusProvider>
    </ThemeProvider>
    </APIProvider>
  );
}

export default App;
