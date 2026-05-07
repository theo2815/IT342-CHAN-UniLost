import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    PlusCircle, GraduationCap, ShieldCheck, BadgeCheck, CheckCircle,
    Package, FileText, ClipboardList, Smile, BellRing,
    ArrowRight, Award, Loader, Edit3, X, Save, AlertCircle
} from 'lucide-react';
import Header from '../../../shared/layout/Header/Header';
import StatusBadge from '../../../shared/components/StatusBadge/StatusBadge';
import EmptyState from '../../../shared/components/EmptyState/EmptyState';
import { Button, Card } from '../../../shared/components/ui';
import authService from '../../auth/authService';
import itemService from '../itemService';
import userService from '../userService';
import claimService from '../claimService';
import { timeAgo } from '../../../shared/utils/timeAgo';
import { PLACEHOLDER_IMAGE } from '../../../shared/constants/images';
import './Profile.css';

function Profile() {
    const navigate = useNavigate();
    const location = useLocation();
    const storedUser = authService.getCurrentUser();
    const tabsRef = useRef(null);

    const [user, setUser] = useState(storedUser);
    const [activeTab, setActiveTab] = useState('LOST');
    const [myItems, setMyItems] = useState([]);
    const [myClaims, setMyClaims] = useState([]);
    const [leaderboard, setLeaderboard] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const tab = queryParams.get('tab');
        if (tab === 'items' || tab === 'claims') {
            if (tab === 'items') {
                setActiveTab('LOST');
            } else if (tab === 'claims') {
                setActiveTab('CLAIMS');
            }
            
            // Wait until data has finished loading before attempting to calculate scroll position
            if (!loading) {
                setTimeout(() => {
                    if (tabsRef.current) {
                        const headerOffset = 90; // Adjust for sticky header
                        const elementPosition = tabsRef.current.getBoundingClientRect().top;
                        const offsetPosition = elementPosition + window.scrollY - headerOffset;
                        window.scrollTo({
                             top: offsetPosition,
                             behavior: "smooth"
                        });
                    }
                }, 150);
            }
        }
    }, [location.search, loading]);

    const fetchProfileData = useCallback(async () => {
        if (!storedUser?.id) return;
        setLoading(true);
        setError('');

        const [userResult, itemsResult, claimsResult, leaderboardResult] = await Promise.all([
            userService.getUserById(storedUser.id),
            itemService.getItemsByUser(storedUser.id, { size: 50 }),
            claimService.getMyClaims(0, 50),
            userService.getLeaderboard(3, storedUser.campus?.id || undefined),
        ]);

        if (userResult.success) {
            setUser(userResult.data);
            localStorage.setItem('user', JSON.stringify(userResult.data));
        } else {
            setError(userResult.error);
        }

        if (itemsResult.success) {
            setMyItems(itemsResult.data.content || []);
        }

        if (claimsResult.success) {
            setMyClaims(claimsResult.data || []);
        }

        if (leaderboardResult.success) {
            setLeaderboard(leaderboardResult.data || []);
        }

        setLoading(false);
    }, [storedUser?.id, storedUser?.campus?.id]);

    useEffect(() => {
        fetchProfileData();
    }, [fetchProfileData]);

    const lostItems = myItems.filter(item => item.type === 'LOST');
    const foundItems = myItems.filter(item => item.type === 'FOUND');

    const getInitials = (name) => {
        const parts = (name || '').split(' ');
        return parts.map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase();
    };

    const roleBadge = () => {
        const labels = { 'STUDENT': 'Student', 'ADMIN': 'Admin' };
        return labels[user?.role] || 'Student';
    };

    const formatMemberSince = (dateString) => {
        if (!dateString) return 'Member';
        const date = new Date(dateString);
        return `Member since ${date.toLocaleString('default', { month: 'long', year: 'numeric' })}`;
    };

    const getKarmaProgress = () => {
        const score = user?.karmaScore || 0;
        const topScore = leaderboard[0]?.karmaScore || 100;
        const maxScore = Math.max(topScore, 100);
        return Math.min((score / maxScore) * 100, 100);
    };

    if (!storedUser) {
        navigate('/login');
        return null;
    }

    if (loading) {
        return (
            <div className="profile-page">
                <Header />
                <main className="profile-container">
                    <div className="profile-loading">
                        <Loader size={32} className="spin" />
                        <p>Loading profile...</p>
                    </div>
                </main>
            </div>
        );
    }

    if (error && !user) {
        return (
            <div className="profile-page">
                <Header />
                <main className="profile-container">
                    <div className="profile-error">
                        <AlertCircle size={32} />
                        <p>{error}</p>
                        <button className="btn-primary-alt" onClick={fetchProfileData}>
                            Try Again
                        </button>
                    </div>
                </main>
            </div>
        );
    }

    const isResolved = (status) => status === 'RETURNED' || status === 'COMPLETED';

    const renderItemList = (items, type) => {
        if (items.length === 0) {
            const isLost = type === 'LOST';
            return (
                <Card glass padded className="empty-state-large">
                    <EmptyState
                        icon={<Smile size={48} />}
                        title={isLost ? 'No lost item reports' : 'No found items yet'}
                        message={isLost
                            ? "You haven't reported any lost items."
                            : 'Help your community! Post any items you\'ve found on campus.'
                        }
                        actionLabel={isLost ? 'Report Lost Item' : 'Post Found Item'}
                        onAction={() => navigate('/post-item')}
                    />
                </Card>
            );
        }

        return items.map((item, index) => (
            <Card
                key={item.id}
                hoverable
                glass
                className={`profile-row-card animate-in${isResolved(item.status) ? ' resolved-item' : ''}`}
                style={{ animationDelay: `${index * 0.05}s` }}
                onClick={() => navigate(`/items/${item.id}`)}
            >
                <div className="item-thumbnail">
                    <img src={item.imageUrls?.[0] || PLACEHOLDER_IMAGE} alt={item.title} />
                    <span className={`type-tag ${type === 'LOST' ? 'report' : 'found'}`}>
                        {type === 'LOST' ? 'REPORT' : 'FOUND'}
                    </span>
                </div>
                <div className="item-details">
                    <div className="item-header-row">
                        <h3>{item.title}</h3>
                        <StatusBadge status={item.status} />
                    </div>
                    <p className="item-snippet">{item.description}</p>
                    <div className="item-footer-meta">
                        <span>{item.category}</span>
                        <span className="dot-mid">&middot;</span>
                        <span>{timeAgo(item.createdAt)}</span>
                    </div>
                </div>
                <div className="item-row-arrow">
                    <ArrowRight size={20} />
                </div>
            </Card>
        ));
    };

    const renderClaims = () => {
        if (myClaims.length === 0) {
            return (
                <Card glass padded className="empty-state-large">
                    <EmptyState
                        icon={<ClipboardList size={48} />}
                        title="No claims yet"
                        message="You haven't submitted any claims. Browse items to find something you lost."
                        actionLabel="Browse Items"
                        onAction={() => navigate('/items')}
                    />
                </Card>
            );
        }

        return myClaims.map((claim, index) => (
            <Card
                key={claim.id}
                hoverable
                glass
                className={`profile-row-card animate-in${isResolved(claim.status) ? ' resolved-item' : ''}`}
                style={{ animationDelay: `${index * 0.05}s` }}
                onClick={() => navigate(`/items/${claim.itemId}`)}
            >
                <div className="item-thumbnail">
                    <img src={claim.itemImageUrl || PLACEHOLDER_IMAGE} alt={claim.itemTitle} />
                    <span className="type-tag claim">CLAIM</span>
                </div>
                <div className="item-details">
                    <div className="item-header-row">
                        <h3>{claim.itemTitle || 'Untitled Item'}</h3>
                        <StatusBadge status={claim.status} />
                    </div>
                    <p className="item-snippet">{claim.message || claim.providedAnswer || 'No message provided'}</p>
                    <div className="item-footer-meta">
                        <span>{claim.itemType}</span>
                        <span className="dot-mid">&middot;</span>
                        <span>{timeAgo(claim.createdAt)}</span>
                    </div>
                </div>
                <div className="item-row-arrow">
                    <ArrowRight size={20} />
                </div>
            </Card>
        ));
    };

    return (
        <div className="profile-page">
            <Header />

            <main className="profile-container">
                {/* Hero Section */}
                <div className="profile-hero-card glass-panel animate-in">
                    <div className="hero-banner banner-pattern">
                        <div className="banner-overlay"></div>
                    </div>

                    <div className="hero-content">
                        <div className="hero-main">
                            <div className="profile-avatar-group">
                                <div className="profile-avatar">
                                    <div className="avatar-placeholder">{getInitials(user.fullName)}</div>
                                </div>
                            </div>

                            <div className="profile-name-section">
                                <div className="name-row">
                                    <h1>{user.fullName}</h1>
                                    <span className="user-tag">
                                        <GraduationCap size={14} />
                                        {roleBadge()}
                                    </span>
                                </div>
                                <p className="user-affiliation">
                                    <ShieldCheck size={16} className="text-gold" />
                                    {user.campus?.name || 'University'}
                                </p>
                                <div className="user-stats-minimal">
                                    <span>{formatMemberSince(user.createdAt)}</span>
                                </div>
                            </div>
                        </div>

                        <div className="hero-actions">
                            <Button variant="secondary" icon={Edit3} onClick={() => navigate('/settings')}>
                                Edit Profile
                            </Button>
                        </div>
                    </div>
                </div>

                {/* Stats Summary */}
                <div className="profile-stats-row animate-in" style={{ animationDelay: '0.1s' }}>
                    <div className="profile-stat-card glass-panel">
                        <span className="stat-number">{lostItems.length}</span>
                        <span className="stat-label">Lost Reports</span>
                    </div>
                    <div className="profile-stat-card glass-panel">
                        <span className="stat-number">{foundItems.length}</span>
                        <span className="stat-label">Found Items</span>
                    </div>
                    <div className="profile-stat-card glass-panel">
                        <span className="stat-number">{myClaims.length}</span>
                        <span className="stat-label">Claims Made</span>
                    </div>
                    <div className="profile-stat-card glass-panel highlight">
                        <span className="stat-number">{user.karmaScore || 0}</span>
                        <span className="stat-label">Karma Points</span>
                    </div>
                </div>

                <div className="profile-grid-layout">
                    {/* Left Column: Account Info & Karma */}
                    <aside className="profile-column column-left">
                        <div className="card glass-panel">
                            <h3 className="card-title">
                                <BadgeCheck className="text-gold" size={18} />
                                Account Info
                            </h3>
                            <div className="info-blocks">
                                <div className="info-block">
                                    <label>Email Address</label>
                                    <div className="info-content">
                                        <p>{user.email}</p>
                                        <CheckCircle size={12} className="text-success" />
                                    </div>
                                </div>
                                <div className="info-block">
                                    <label>Role</label>
                                    <p>{roleBadge()}</p>
                                </div>
                                <div className="info-block">
                                    <label>Campus</label>
                                    <p>{user.campus?.name || 'Not assigned'}</p>
                                </div>
                                <div className="info-block">
                                    <label>Account Status</label>
                                    <div>
                                        <StatusBadge status={user.accountStatus || 'ACTIVE'} />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="card glass-panel relative overflow-hidden">
                            <h3 className="card-title">Karma Score</h3>
                            <div className="karma-display">
                                <div className="karma-ring-container">
                                    <div className="karma-ring-progress" style={{ '--progress': `${getKarmaProgress()}%` }}>
                                        <div className="karma-ring-inner">
                                            <span className="karma-value">{user.karmaScore || 0}</span>
                                            <span className="karma-label">Points</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            {leaderboard.length > 0 && (
                                <div className="karma-leaderboard">
                                    <p className="section-subtitle">Campus Top {leaderboard.length}</p>
                                    <div className="mini-leaderboard">
                                        {leaderboard.map((entry, index) => (
                                            <div key={entry.id} className={`mini-user-row ${entry.id === user.id ? 'active' : ''}`}>
                                                <div className={`user-rank rank-${index + 1}`}>{index + 1}</div>
                                                <div className="user-avatar-small-initials">
                                                    {getInitials(entry.fullName)}
                                                </div>
                                                <span className="user-name">
                                                    {entry.id === user.id ? 'You' : entry.fullName}
                                                </span>
                                                <span className={`user-points ${entry.id === user.id ? 'text-gold font-bold' : ''}`}>
                                                    {entry.karmaScore} pts
                                                </span>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                            <Award className="card-bg-icon" size={80} />
                        </div>
                    </aside>

                    {/* Main Column: Tabs & Content */}
                    <div className="profile-column column-center">
                        <div className="profile-main-tabs glass-panel" ref={tabsRef}>
                            <button
                                className={`main-tab ${activeTab === 'LOST' ? 'active' : ''}`}
                                onClick={() => setActiveTab('LOST')}
                            >
                                <Package size={18} />
                                Lost Items ({lostItems.length})
                            </button>
                            <button
                                className={`main-tab ${activeTab === 'FOUND' ? 'active' : ''}`}
                                onClick={() => setActiveTab('FOUND')}
                            >
                                <FileText size={18} />
                                Found Items ({foundItems.length})
                            </button>
                            <button
                                className={`main-tab ${activeTab === 'CLAIMS' ? 'active' : ''}`}
                                onClick={() => setActiveTab('CLAIMS')}
                            >
                                <ClipboardList size={18} />
                                My Claims ({myClaims.length})
                            </button>
                        </div>

                        <div className="profile-list-container">
                            {activeTab === 'LOST' && renderItemList(lostItems, 'LOST')}
                            {activeTab === 'FOUND' && renderItemList(foundItems, 'FOUND')}
                            {activeTab === 'CLAIMS' && renderClaims()}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default Profile;
