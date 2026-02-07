import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { GraduationCap, ChevronDown, User, Settings, LogOut, Bell } from 'lucide-react';
import authService from '../services/authService';
import './Header.css';

function Header() {
    const navigate = useNavigate();
    const [user] = useState(() => authService.getCurrentUser());
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef(null);

    useEffect(() => {
        // Close dropdown when clicking outside
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsDropdownOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleLogout = () => {
        authService.logout();
        navigate('/login');
    };

    const toggleDropdown = () => {
        setIsDropdownOpen(!isDropdownOpen);
    };

    // Get initials for avatar
    const getInitials = () => {
        if (!user) return 'G';
        return `${user.firstName?.charAt(0) || ''}${user.lastName?.charAt(0) || ''}`.toUpperCase();
    };

    return (
        <header className="main-header glass">
            <div className="header-left">
                <Link to="/dashboard" className="logo">
                    <GraduationCap className="logo-icon" size={28} />
                    <span className="logo-text">HulamPay</span>
                </Link>
            </div>

            <div className="header-right">


                <button className="icon-btn notification-btn">
                    <Bell size={20} />
                    <span className="notification-badge">3</span>
                </button>

                <div className="user-menu" ref={dropdownRef}>
                    <button className="user-btn" onClick={toggleDropdown}>
                        <div className="user-avatar">
                            {getInitials()}
                        </div>
                        <span className="user-name">
                            {user ? `Hello, ${user.firstName}` : 'Guest'}
                        </span>
                        <ChevronDown size={16} className={`chevron ${isDropdownOpen ? 'open' : ''}`} />
                    </button>

                    {isDropdownOpen && (
                        <div className="dropdown-menu glass">
                            <div className="dropdown-header">
                                <p className="dropdown-name">{user ? `${user.firstName} ${user.lastName}` : 'Guest User'}</p>
                                <p className="dropdown-email">{user?.email || ''}</p>
                            </div>
                            <div className="dropdown-divider"></div>
                            <Link to="/profile" className="dropdown-item" onClick={() => setIsDropdownOpen(false)}>
                                <User size={18} />
                                <span>Profile</span>
                            </Link>
                            <Link to="/settings" className="dropdown-item" onClick={() => setIsDropdownOpen(false)}>
                                <Settings size={18} />
                                <span>Settings</span>
                            </Link>
                            <div className="dropdown-divider"></div>
                            <button className="dropdown-item logout" onClick={handleLogout}>
                                <LogOut size={18} />
                                <span>Logout</span>
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </header>
    );
}

export default Header;
