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
import authService from "../services/authService";
import NotificationDropdown from "./NotificationDropdown";
import { getUnreadCount } from "../mockData/notifications";
import "./Header.css";

function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user] = useState(() => authService.getCurrentUser());
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(() => getUnreadCount());
  const dropdownRef = useRef(null);
  const notificationRef = useRef(null);
  const isAdmin = authService.isAdmin();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
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

  const handleLogout = () => {
    authService.logout();
    navigate("/login");
  };

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };

  const getInitials = () => {
    if (!user) return "G";
    return `${user.firstName?.charAt(0) || ""}${user.lastName?.charAt(0) || ""}`.toUpperCase();
  };

  return (
    <header className="main-header glass">
      <div className="header-left">
        <Link to={user ? "/dashboard" : "/login"} className="logo">
          <Search className="logo-icon" size={28} />
          <span className="logo-text">UniLost</span>
        </Link>
      </div>

      <nav className="header-nav">
        <Link
          to={user ? "/dashboard" : "/login"}
          className={`nav-link ${location.pathname === "/dashboard" ? "active" : ""}`}
        >
          <Home size={16} />
          Home
        </Link>
        <Link
          to={user ? "/items" : "/login"}
          className={`nav-link ${location.pathname === "/items" ? "active" : ""}`}
        >
          <Search size={16} />
          Item Feed
        </Link>
        <Link
          to={user ? "/map" : "/login"}
          className={`nav-link ${location.pathname === "/map" ? "active" : ""}`}
        >
          <Map size={16} />
          Map View
        </Link>
        <Link
          to={user ? "/leaderboard" : "/login"}
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
            </Link>

            <div className="notification-wrapper" ref={notificationRef}>
              <button
                className="icon-btn notification-btn"
                onClick={() => setIsNotificationOpen(!isNotificationOpen)}
              >
                <Bell size={20} />
                {unreadCount > 0 && (
                  <span className="notification-badge">{unreadCount}</span>
                )}
              </button>
              {isNotificationOpen && (
                <NotificationDropdown
                  onClose={() => setIsNotificationOpen(false)}
                  onCountChange={() => setUnreadCount(getUnreadCount())}
                />
              )}
            </div>

            <Link
              to="/post-item"
              className="btn btn-primary header-post-btn"
              style={{ gap: "0.5rem", display: "flex", alignItems: "center" }}
            >
              <PlusCircle size={16} />
              <span className="desktop-only text-sm font-bold">Post Item</span>
            </Link>

            <div className="user-menu" ref={dropdownRef}>
              <button className="user-btn" onClick={toggleDropdown}>
                <div className="user-avatar">{getInitials()}</div>
                <span className="user-name">
                  {user ? `Hello, ${user.firstName}` : "Guest"}
                </span>
                <ChevronDown
                  size={16}
                  className={`chevron ${isDropdownOpen ? "open" : ""}`}
                />
              </button>

              {isDropdownOpen && (
                <div className="dropdown-menu glass">
                  <div className="dropdown-header">
                    <p className="dropdown-name">
                      {user
                        ? `${user.firstName} ${user.lastName}`
                        : "Guest User"}
                    </p>
                    <p className="dropdown-email">{user?.email || ""}</p>
                    {user?.role && user.role !== "STUDENT" && (
                      <span className="dropdown-role">
                        {user.role.replace("_", " ")}
                      </span>
                    )}
                  </div>
                  <div className="dropdown-divider"></div>
                  <Link
                    to="/profile"
                    className="dropdown-item"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <User size={18} />
                    <span>Profile</span>
                  </Link>
                  <Link
                    to="/settings"
                    className="dropdown-item"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <Settings size={18} />
                    <span>Settings</span>
                  </Link>
                  <Link
                    to="/profile"
                    className="dropdown-item"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <Package size={18} />
                    <span>My Items</span>
                  </Link>
                  <Link
                    to="/profile"
                    className="dropdown-item"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <FileText size={18} />
                    <span>My Claims</span>
                  </Link>
                  {isAdmin && (
                    <>
                      <div className="dropdown-divider"></div>
                      <Link
                        to="/admin"
                        className="dropdown-item"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        <Shield size={18} />
                        <span>Admin Panel</span>
                      </Link>
                    </>
                  )}
                  <div className="dropdown-divider"></div>
                  <button
                    className="dropdown-item logout"
                    onClick={handleLogout}
                  >
                    <LogOut size={18} />
                    <span>Logout</span>
                  </button>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </header>
  );
}

export default Header;
