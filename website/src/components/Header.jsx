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
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import authService from "../services/authService";
import chatService from "../services/chatService";
import notificationService from "../services/notificationService";
import NotificationDropdown from "./NotificationDropdown";
import "./Header.css";

const WS_URL = import.meta.env.VITE_API_URL?.replace("/api", "") || "http://localhost:8080";

function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user] = useState(() => authService.getCurrentUser());
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [unreadMessages, setUnreadMessages] = useState(0);
  const dropdownRef = useRef(null);
  const notificationRef = useRef(null);
  const stompClientRef = useRef(null);
  const isAdmin = authService.isAdmin();

  // Fetch unread counts from real API
  useEffect(() => {
    if (!user) return;

    const fetchUnread = () => {
      chatService.getUnreadCount().then(result => {
        if (result.success) setUnreadMessages(result.data);
      });
      notificationService.getUnreadCount().then(result => {
        if (result.success) setUnreadCount(result.data);
      });
    };

    fetchUnread();

    // Poll every 30 seconds, but only when tab is visible
    const interval = setInterval(() => {
      if (document.visibilityState === "visible") {
        fetchUnread();
      }
    }, 30000);

    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible") fetchUnread();
    };
    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      clearInterval(interval);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, []);

  // WebSocket subscription for real-time notifications
  useEffect(() => {
    if (!user) return;

    const token = localStorage.getItem("token");
    if (!token) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_URL}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe("/user/queue/notifications", (msg) => {
          // Increment unread count on new notification
          setUnreadCount((prev) => prev + 1);
        });
      },
      onStompError: () => {},
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (stompClientRef.current?.active) {
        stompClientRef.current.deactivate();
      }
    };
  }, []);

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
    if (stompClientRef.current?.active) {
      stompClientRef.current.deactivate();
    }
    authService.logout();
    navigate("/login");
  };

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };

  const refreshUnreadCount = () => {
    notificationService.getUnreadCount().then(result => {
      if (result.success) setUnreadCount(result.data);
    });
  };

  const getInitials = () => {
    if (!user?.fullName) return "G";
    return user.fullName.split(' ').map(n => n.charAt(0)).join('').substring(0, 2).toUpperCase();
  };

  return (
    <header className="main-header glass">
      <div className="header-left">
        <Link to={user ? "/dashboard" : "/"} className="logo">
          <Search className="logo-icon" size={28} />
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
                {unreadCount > 0 && (
                  <span className="notification-badge">{unreadCount}</span>
                )}
              </button>
              {isNotificationOpen && (
                <NotificationDropdown
                  onClose={() => setIsNotificationOpen(false)}
                  onCountChange={refreshUnreadCount}
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
                  {user ? `Hello, ${user.fullName?.split(' ')[0] || 'User'}` : "Guest"}
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
                      {user?.fullName || "Guest User"}
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
                    to="/my-claims"
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
