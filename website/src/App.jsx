import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login/Login';
import Register from './pages/Register/Register';
import Dashboard from './pages/Dashboard/Dashboard';
import Profile from './pages/Profile/Profile';
import Settings from './pages/Settings/Settings';
import ItemFeed from './pages/ItemFeed/ItemFeed';
import ItemDetail from './pages/ItemDetail/ItemDetail';
import PostItem from './pages/PostItem/PostItem';
import MyItems from './pages/MyItems/MyItems';
import MyClaims from './pages/MyClaims/MyClaims';
import IncomingClaims from './pages/IncomingClaims/IncomingClaims';
import ClaimDetail from './pages/ClaimDetail/ClaimDetail';
import Notifications from './pages/Notifications/Notifications';
import AdminDashboard from './pages/Admin/AdminDashboard';
import AdminItems from './pages/Admin/AdminItems';
import AdminUsers from './pages/Admin/AdminUsers';
import AdminClaims from './pages/Admin/AdminClaims';
import SuperAdminPanel from './pages/Admin/SuperAdminPanel';
import ProtectedRoute from './components/ProtectedRoute';
import { AdminRoute, SuperAdminRoute } from './components/AdminRoute';
import './App.css';

import { ThemeProvider } from './context/ThemeContext';

function App() {
  return (
    <ThemeProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/items" element={<ItemFeed />} />
            <Route path="/items/:id" element={<ItemDetail />} />
            <Route path="/post-item" element={<PostItem />} />
            <Route path="/my-items" element={<MyItems />} />
            <Route path="/my-items/:itemId/claims" element={<IncomingClaims />} />
            <Route path="/my-claims" element={<MyClaims />} />
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
            <Route element={<SuperAdminRoute />}>
              <Route path="/superadmin" element={<SuperAdminPanel />} />
            </Route>
          </Route>
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;
