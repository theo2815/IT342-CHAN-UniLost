import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    PlusCircle,
    Image as ImageIcon, Camera, Check,
    GraduationCap, ShieldCheck, BadgeCheck, CheckCircle,
    Package, FileText, History, Handshake, Smile, Search,
    BellRing, Heart, Compass, Flame, Lock, Users,
    Send, ArrowRight,
    Award, MoreHorizontal
} from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import authService from '../../services/authService';
import itemService from '../../services/itemService';
import { timeAgo } from '../../utils/timeAgo';
import './Profile.css';

function Profile() {
    const navigate = useNavigate();
    const [user] = useState(() => authService.getCurrentUser());
    const [activeTab, setActiveTab] = useState('LOST');
    const [myItems, setMyItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchMyItems = async () => {
            if (!user?.id) return;
            setLoading(true);
            setError('');
            const result = await itemService.getItemsByUser(user.id, { size: 50 });
            if (result.success) {
                setMyItems(result.data.content || []);
            } else {
                setError(result.error);
            }
            setLoading(false);
        };
        fetchMyItems();
    }, [user]);

    const lostItems = myItems.filter(item => item.type === 'LOST');
    const foundItems = myItems.filter(item => item.type === 'FOUND');

    if (!user) {
        return <div className="loading">Loading profile...</div>;
    }

    const getInitials = () => {
        const parts = (user.fullName || '').split(' ');
        return parts.map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase();
    };

    const roleBadge = () => {
        const role = user.role || 'STUDENT';
        const labels = {
            'STUDENT': 'Student',
            'FACULTY': 'Faculty',
            'ADMIN': 'Admin'
        };
        return labels[role] || 'Student';
    };

    return (
        <div className="profile-page">
            <Header />
            
            <main className="profile-container">
                {/* Hero / Header Section */}
                <div className="profile-hero-card glass-panel animate-in">
                    <div className="hero-banner banner-pattern">
                        <div className="banner-overlay"></div>
                        <button className="change-cover-btn">
                            <ImageIcon size={14} />
                            Change Cover
                        </button>
                    </div>
                    
                    <div className="hero-content">
                        <div className="hero-main">
                            <div className="profile-avatar-group">
                                <div className="profile-avatar">
                                    <div className="avatar-placeholder">{getInitials()}</div>
                                    <div className="avatar-camera-overlay">
                                        <Camera size={24} color="white" />
                                    </div>
                                </div>
                                <div className="online-status-indicator" title="Online">
                                    <Check size={12} strokeWidth={4} />
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
                                    Verified Student &bull; {user.campus?.name || 'Cebu Institute of Technology - University'}
                                </p>
                                <div className="user-stats-minimal">
                                    <span>Member since 2024</span>
                                    <span className="status-dot-group">
                                        <span className="dot dot-success"></span>
                                        Online Now
                                    </span>
                                </div>
                            </div>
                        </div>
                        
                        <div className="hero-actions">
                            <button className="btn-secondary-alt">Share Profile</button>
                            <button className="btn-primary-alt">
                                <PlusCircle size={18} />
                                Edit Profile
                            </button>
                        </div>
                    </div>
                </div>

                <div className="profile-grid-layout">
                    {/* Left Column: Student Info & Goals */}
                    <aside className="profile-column column-left">
                        <div className="card glass-panel">
                            <h3 className="card-title">
                                <BadgeCheck className="text-gold" size={18} />
                                Student Info
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
                                    <label>Student ID</label>
                                    <p className="font-mono">{user.studentIdNumber || '23-2578-976'}</p>
                                </div>
                                <div className="info-block">
                                    <label>Department</label>
                                    <p>College of Computer Studies</p>
                                </div>
                            </div>
                        </div>

                        <div className="card glass-panel relative overflow-hidden">
                            <div className="card-header-flex">
                                <h3 className="card-title">Karma Level</h3>
                                <button className="text-link">
                                    History <ArrowRight size={12} />
                                </button>
                            </div>
                            <div className="karma-display">
                                <div className="karma-ring-container">
                                    <div className="karma-ring-progress" style={{ '--progress': '85%' }}>
                                        <div className="karma-ring-inner">
                                            <span className="karma-value">{user.karmaScore ?? 750}</span>
                                            <span className="karma-label">Points</span>
                                        </div>
                                    </div>
                                </div>
                                <p className="karma-desc">You are in the top <strong className="text-gold">15%</strong> of finders at your university!</p>
                            </div>
                            <div className="karma-leaderboard">
                                <p className="section-subtitle">Campus Top 3</p>
                                <div className="mini-leaderboard">
                                    <div className="mini-user-row">
                                        <div className="user-rank rank-1">1</div>
                                        <div className="user-avatar-small"></div>
                                        <span className="user-name">Maria S.</span>
                                        <span className="user-points">1250 pts</span>
                                    </div>
                                    <div className="mini-user-row">
                                        <div className="user-rank rank-2">2</div>
                                        <div className="user-avatar-small"></div>
                                        <span className="user-name">John D.</span>
                                        <span className="user-points">980 pts</span>
                                    </div>
                                    <div className="mini-user-row active">
                                        <div className="user-rank rank-3">3</div>
                                        <div className="user-avatar-small"></div>
                                        <span className="user-name">You</span>
                                        <span className="user-points text-gold font-bold">{user.karmaScore ?? 750} pts</span>
                                    </div>
                                </div>
                            </div>
                            <Award className="card-bg-icon" size={80} />
                        </div>

                        <div className="card glass-panel">
                            <div className="card-header-flex">
                                <h3 className="card-title">Monthly Goals</h3>
                                <MoreHorizontal size={16} />
                            </div>
                            <div className="goals-list">
                                <div className="goal-item">
                                    <div className="goal-label-row">
                                        <span className="goal-name">Items Returned</span>
                                        <span className="goal-count">3 / 5</span>
                                    </div>
                                    <div className="progress-bar">
                                        <div className="progress-fill bg-success" style={{ width: '60%' }}></div>
                                    </div>
                                </div>
                                <div className="goal-item">
                                    <div className="goal-label-row">
                                        <span className="goal-name">Verify Lost Items</span>
                                        <span className="goal-count">8 / 10</span>
                                    </div>
                                    <div className="progress-bar">
                                        <div className="progress-fill bg-accent" style={{ width: '80%' }}></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </aside>

                    {/* Center Column: Main Content */}
                    <div className="profile-column column-center">
                        <div className="profile-main-tabs glass-panel">
                            <button 
                                className={`main-tab ${activeTab === 'LOST' ? 'active' : ''}`}
                                onClick={() => setActiveTab('LOST')}
                            >
                                <Package size={18} />
                                Lost Items
                            </button>
                            <button 
                                className={`main-tab ${activeTab === 'FOUND' ? 'active' : ''}`}
                                onClick={() => setActiveTab('FOUND')}
                            >
                                <FileText size={18} />
                                Found Items
                            </button>
                            <button className="main-tab">
                                <History size={18} />
                                Activity
                            </button>
                        </div>

                        <div className="card glass-panel no-padding">
                            <div className="card-p-header">
                                <h4 className="text-sm font-bold">Recent Handover Activity</h4>
                                <span className="text-muted text-xs">Last 7 days</span>
                            </div>
                            <div className="handover-list">
                                <div className="handover-item">
                                    <div className="handover-icon bg-success-light">
                                        <Handshake size={20} className="text-success" />
                                    </div>
                                    <div className="handover-info">
                                        <p>
                                            <span className="font-bold">You</span> successfully returned a <span className="font-bold">Casio Calculator</span> to <span className="text-accent font-bold">Mark V.</span>
                                        </p>
                                        <span className="text-xs text-muted">2 days ago &bull; Library Lobby</span>
                                    </div>
                                </div>
                                <div className="handover-item border-top">
                                    <div className="handover-icon bg-accent-light">
                                        <Search size={20} className="text-accent" />
                                    </div>
                                    <div className="handover-info">
                                        <p>
                                            <span className="font-bold">You</span> reported a lost <span className="font-bold">Hydroflask (Black)</span>.
                                        </p>
                                        <span className="text-xs text-muted">5 days ago &bull; Canteen Area</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {activeTab === 'LOST' ? (
                            <div className="profile-list-container">
                                {lostItems.length > 0 ? (
                                    lostItems.map((item, index) => (
                                        <div key={item.id} className="profile-row-card glass-panel animate-in" style={{ animationDelay: `${index * 0.05}s` }}>
                                            <div className="item-thumbnail">
                                                <img src={item.imageUrls?.[0] || 'https://picsum.photos/seed/placeholder/100/100'} alt={item.title} />
                                                <span className="type-tag report">REPORT</span>
                                            </div>
                                            <div className="item-details" onClick={() => navigate(`/items/${item.id}`)}>
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
                                                <ArrowRight size={20} onClick={() => navigate(`/items/${item.id}`)} />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="empty-state-large glass-panel">
                                        <div className="empty-icon-circle">
                                            <Smile size={40} />
                                        </div>
                                        <h2>No active lost items</h2>
                                        <p>That's great news! You haven't reported any lost items recently.</p>
                                        <button className="btn-primary-alt" onClick={() => navigate('/post-item')}>
                                            <BellRing size={18} />
                                            Report Lost Item
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="profile-list-container">
                                {foundItems.length > 0 ? (
                                    foundItems.map((item, index) => (
                                        <div key={item.id} className="profile-row-card glass-panel animate-in" style={{ animationDelay: `${index * 0.05}s` }}>
                                            <div className="item-thumbnail">
                                                <img src={item.imageUrls?.[0] || 'https://picsum.photos/seed/placeholder/100/100'} alt={item.title} />
                                                <span className="type-tag found">FOUND</span>
                                            </div>
                                            <div className="item-details" onClick={() => navigate(`/items/${item.id}`)}>
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
                                                <ArrowRight size={20} onClick={() => navigate(`/items/${item.id}`)} />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="empty-state-large glass-panel">
                                        <div className="empty-icon-circle">
                                            <Smile size={40} />
                                        </div>
                                        <h2>No found items yet</h2>
                                        <p>Help your community! Post any items you've found on campus.</p>
                                        <button className="btn-primary-alt" onClick={() => navigate('/post-item')}>
                                            <PlusCircle size={18} />
                                            Post Found Item
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Right Column: Insights & Badges */}
                    <aside className="profile-column column-right">
                        <div className="card glass-panel">
                            <div className="card-header-flex">
                                <h3 className="card-title">Collectibles</h3>
                                <button className="text-link text-xs">View All</button>
                            </div>
                            <div className="badges-grid">
                                <div className="badge-item tool-tip" data-tip="Helpful Hand: Returned 5 items">
                                    <div className="badge-icon bg-blue-tint">
                                        <Heart size={20} className="text-accent" />
                                    </div>
                                </div>
                                <div className="badge-item tool-tip" data-tip="Explorer: Found items in 3 zones">
                                    <div className="badge-icon bg-purple-tint">
                                        <Compass size={20} className="text-purple" />
                                    </div>
                                </div>
                                <div className="badge-item tool-tip" data-tip="Hot Streak: Active 7 days straight">
                                    <div className="badge-icon bg-orange-tint">
                                        <Flame size={20} className="text-gold" />
                                    </div>
                                </div>
                                <div className="badge-item locked">
                                    <div className="badge-icon bg-locked">
                                        <Lock size={20} className="text-muted" />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="card glass-panel">
                            <h3 className="card-title">Activity Insights</h3>
                            <div className="insight-rows">
                                <div className="insight-row group">
                                    <div className="insight-icon bg-success-light">
                                        <CheckCircle size={20} />
                                    </div>
                                    <div>
                                        <p className="insight-value">100%</p>
                                        <p className="insight-label">Recovery Rate</p>
                                    </div>
                                </div>
                                <div className="insight-row group">
                                    <div className="insight-icon bg-accent-light">
                                        <Users size={20} />
                                    </div>
                                    <div>
                                        <p className="insight-value">12</p>
                                        <p className="insight-label">Community Helps</p>
                                    </div>
                                </div>
                            </div>
                            <div className="weekly-chart-section">
                                <div className="chart-header">
                                    <p className="chart-title">Weekly Activity</p>
                                    <span className="chart-trend">+15%</span>
                                </div>
                                <div className="bar-chart">
                                    <div className="chart-bar" style={{ height: '30%' }} title="Mon"></div>
                                    <div className="chart-bar" style={{ height: '50%' }} title="Tue"></div>
                                    <div className="chart-bar active" style={{ height: '80%' }} title="Wed">
                                        <div className="chart-tooltip">High Activity</div>
                                    </div>
                                    <div className="chart-bar" style={{ height: '40%' }} title="Thu"></div>
                                    <div className="chart-bar" style={{ height: '20%' }} title="Fri"></div>
                                    <div className="chart-bar" style={{ height: '60%' }} title="Sat"></div>
                                    <div className="chart-bar" style={{ height: '45%' }} title="Sun"></div>
                                </div>
                                <div className="chart-labels">
                                    <span>M</span><span>T</span><span>W</span><span>T</span><span>F</span><span>S</span><span>S</span>
                                </div>
                            </div>
                        </div>

                        <div className="card promo-card glass-panel animate-pulse-subtle">
                            <div className="promo-badge">Nearby Alert</div>
                            <h4 className="promo-title">Help the Community</h4>
                            <p className="promo-desc">There are <strong className="text-gold">3 lost items</strong> reported near the CIT Library in the last hour.</p>
                            <button className="btn-glass">
                                <Send size={16} />
                                View Nearby Items
                            </button>
                            <Handshake size={100} className="promo-bg-icon" />
                        </div>
                    </aside>
                </div>
            </main>
        </div>
    );
}

export default Profile;
