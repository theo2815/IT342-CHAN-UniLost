import { useState, useEffect, useRef } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import {
  Search,
  ChevronDown,
  User,
  Settings,
  LogOut,
  Bell,
  Shield,
  Package,
  FileText,
  Home,
  Map,
  Trophy,
  MessageSquare,
  PlusCircle,
} from "lucide-react";
import authService from "../../../features/auth/authService";
import { useUnread } from "../../context/UnreadContext";
import { Dropdown } from "../../components/ui";
import NotificationDropdown from "../../../features/user/NotificationDropdown/NotificationDropdown";
import "./Header.css";

function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState(() => authService.getCurrentUser());
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);
  const notificationRef = useRef(null);
  const isAdmin = user?.role === 'ADMIN';

  const {
    unreadNotifications,
    unreadMessages,
    refreshNotificationCount,
    handleLogout: cleanupWs,
  } = useUnread();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        notificationRef.current &&
        !notificationRef.current.contains(event.target)
      ) {
        setIsNotificationOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  useEffect(() => {
    const handleProfileUpdate = () => {
      setUser(authService.getCurrentUser());
    };
    window.addEventListener('userProfileUpdated', handleProfileUpdate);
    return () => {
      window.removeEventListener('userProfileUpdated', handleProfileUpdate);
    };
  }, []);

  useEffect(() => {
    let mounted = true;

    const syncUser = async () => {
      if (!authService.isAuthenticated()) {
        if (mounted) {
          setUser(null);
        }
        return;
      }

      const result = await authService.syncCurrentUser();
      if (mounted && result.success) {
        setUser(result.data);
      }
    };

    syncUser();
    return () => {
      mounted = false;
    };
  }, []);

  const handleLogout = () => {
    cleanupWs();
    authService.logout();
    navigate("/login");
  };

  const getInitials = () => {
    if (!user?.fullName) return "G";
    return user.fullName.split(' ').map(n => n.charAt(0)).join('').substring(0, 2).toUpperCase();
  };

  return (
    <header className="main-header glass">
      <div className="header-left">
        <Link to={user ? "/dashboard" : "/"} className="logo">
          <img src="/unilost-logo.png" alt="UniLost" className="logo-icon" />
          <span className="logo-text">UniLost</span>
        </Link>
      </div>

      <nav className="header-nav">
        {user && (
          <Link
            to="/dashboard"
            className={`nav-link ${location.pathname === "/dashboard" ? "active" : ""}`}
          >
            <Home size={16} />
            Home
          </Link>
        )}
        <Link
          to="/items"
          className={`nav-link ${location.pathname === "/items" ? "active" : ""}`}
        >
          <Search size={16} />
          Item Feed
        </Link>
        <Link
          to="/map"
          className={`nav-link ${location.pathname === "/map" ? "active" : ""}`}
        >
          <Map size={16} />
          Map View
        </Link>
        <Link
          to="/leaderboard"
          className={`nav-link ${location.pathname === "/leaderboard" ? "active" : ""}`}
        >
          <Trophy size={16} />
          Leaderboard
        </Link>
      </nav>

      <div className="header-right">
        {!user ? (
          <div className="header-auth">
            <Link to="/login" className="btn btn-outline">
              Login
            </Link>
            <Link to="/register" className="btn btn-primary">
              Register
            </Link>
          </div>
        ) : (
          <>
            <Link to="/messages" className="icon-btn message-btn">
              <MessageSquare size={20} />
              {unreadMessages > 0 && (
                <span className="message-badge">{unreadMessages}</span>
              )}
            </Link>

            <div className="notification-wrapper" ref={notificationRef}>
              <button
                className="icon-btn notification-btn"
                onClick={() => setIsNotificationOpen(!isNotificationOpen)}
              >
                <Bell size={20} />
                {localStorage.getItem('notificationsEnabled') !== 'false' && unreadNotifications > 0 && (
                  <span className="notification-badge">{unreadNotifications}</span>
                )}
              </button>
              {isNotificationOpen && (
                <NotificationDropdown
                  onClose={() => setIsNotificationOpen(false)}
                  onCountChange={refreshNotificationCount}
                />
              )}
            </div>

            <Link
              to="/post-item"
              className="btn btn-primary header-post-btn"
            >
              <PlusCircle size={16} />
              <span className="desktop-only text-sm font-bold">Post Item</span>
            </Link>

            <Dropdown
              trigger={(isOpen) => (
                <button className="user-btn">
                  <div className="user-avatar">
                    {user?.profilePictureUrl ? (
                      <img src={user.profilePictureUrl} alt={user.fullName} className="user-avatar-img" />
                    ) : (
                      getInitials()
                    )}
                  </div>
                  <span className="user-name">
                    {user ? `Hello, ${user.fullName?.split(' ')[0] || 'User'}` : "Guest"}
                  </span>
                  <ChevronDown size={16} className={`chevron ${isOpen ? 'open' : ''}`} />
                </button>
              )}
              align="right"
              width={240}
              className="user-menu"
            >
              {({ close }) => (
                <>
                  <Dropdown.Header>
                    <p className="dropdown-name">
                      {user?.fullName || "Guest User"}
                    </p>
                    <p className="dropdown-email">{user?.email || ""}</p>
                    {user?.role === "ADMIN" && (
                      <span className="dropdown-role">Admin</span>
                    )}
                  </Dropdown.Header>
                  <Dropdown.Divider />
                  <Link
                    to="/profile"
                    className="ui-dropdown-item"
                    onClick={close}
                  >
                    <User size={18} />
                    <span>Profile</span>
                  </Link>
                  <Link
                    to="/settings"
                    className="ui-dropdown-item"
                    onClick={close}
                  >
                    <Settings size={18} />
                    <span>Settings</span>
                  </Link>
                  <Link
                    to="/profile?tab=items"
                    className="ui-dropdown-item"
                    onClick={close}
                  >
                    <Package size={18} />
                    <span>My Items</span>
                  </Link>
                  <Link
                    to="/profile?tab=claims"
                    className="ui-dropdown-item"
                    onClick={close}
                  >
                    <FileText size={18} />
                    <span>My Claims</span>
                  </Link>
                  {isAdmin && (
                    <>
                      <Dropdown.Divider />
                      <Link
                        to="/admin"
                        className="ui-dropdown-item"
                        onClick={close}
                      >
                        <Shield size={18} />
                        <span>Admin Panel</span>
                      </Link>
                    </>
                  )}
                  <Dropdown.Divider />
                  <Dropdown.Item variant="danger" onClick={handleLogout}>
                    <LogOut size={18} />
                    <span>Logout</span>
                  </Dropdown.Item>
                </>
              )}
            </Dropdown>
          </>
        )}
      </div>
    </header>
  );
}

export default Header;
