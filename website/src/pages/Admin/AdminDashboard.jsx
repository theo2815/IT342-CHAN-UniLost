import { useNavigate } from 'react-router-dom';
import { Shield, Users, Package, Clock, CheckCircle, Ban, Trash2, UserX, UserCheck, Gavel, Settings, ChevronRight } from 'lucide-react';
import Header from '../../components/Header';
import { mockAdminStats, mockAdminActions, timeAgo } from '../../mockData/adminData';
import authService from '../../services/authService';
import './AdminDashboard.css';

const actionIcons = {
    ITEM_REMOVAL: { icon: Trash2, color: '#ef4444' },
    USER_BAN: { icon: UserX, color: '#ef4444' },
    USER_UNBAN: { icon: UserCheck, color: '#10b981' },
    CLAIM_OVERRIDE: { icon: Gavel, color: '#f59e0b' },
    SCHOOL_UPDATE: { icon: Settings, color: '#6366f1' },
};

const AdminDashboard = () => {
    const navigate = useNavigate();
    const role = authService.getUserRole();

    const statCards = [
        { label: 'Total Users', value: mockAdminStats.totalUsers, icon: Users, color: '#3b82f6' },
        { label: 'Active Items', value: mockAdminStats.activeItems, icon: Package, color: '#10b981' },
        { label: 'Pending Claims', value: mockAdminStats.pendingClaims, icon: Clock, color: '#f59e0b' },
        { label: 'Recovered This Month', value: mockAdminStats.recoveredThisMonth, icon: CheckCircle, color: '#059669' },
        { label: 'Banned Users', value: mockAdminStats.bannedUsers, icon: Ban, color: '#ef4444' },
    ];

    const quickLinks = [
        { label: 'Items Management', description: 'View, filter, and remove item listings', route: '/admin/items', icon: Package },
        { label: 'Users Management', description: 'Search, ban, and manage user accounts', route: '/admin/users', icon: Users },
        { label: 'Claims Management', description: 'Oversee claims and override handovers', route: '/admin/claims', icon: Gavel },
    ];

    return (
        <div className="admin-dashboard-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="page-header">
                        <div className="page-header-top">
                            <Shield size={28} />
                            <h1>Admin Dashboard</h1>
                            <span className={`role-badge ${role === 'SUPER_ADMIN' ? 'super' : ''}`}>
                                {role === 'SUPER_ADMIN' ? 'Super Admin' : 'Campus Admin'}
                            </span>
                        </div>
                        <p>Manage items, users, and claims across the platform</p>
                    </div>

                    {/* Stat Cards */}
                    <div className="stat-grid">
                        {statCards.map((stat, i) => (
                            <div className="stat-card glass" key={i} style={{ animationDelay: `${i * 0.05}s` }}>
                                <div className="stat-icon" style={{ background: `${stat.color}15`, color: stat.color }}>
                                    <stat.icon size={22} />
                                </div>
                                <div className="stat-info">
                                    <span className="stat-value">{stat.value}</span>
                                    <span className="stat-label">{stat.label}</span>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Quick Links */}
                    <h2 className="section-title">Management</h2>
                    <div className="quick-links">
                        {quickLinks.map((link, i) => (
                            <div
                                className="quick-link-card glass"
                                key={i}
                                onClick={() => navigate(link.route)}
                                style={{ animationDelay: `${(i + 5) * 0.05}s` }}
                            >
                                <div className="ql-icon">
                                    <link.icon size={24} />
                                </div>
                                <div className="ql-info">
                                    <h3>{link.label}</h3>
                                    <p>{link.description}</p>
                                </div>
                                <ChevronRight size={20} className="ql-arrow" />
                            </div>
                        ))}

                        {role === 'SUPER_ADMIN' && (
                            <div
                                className="quick-link-card glass super-admin-link"
                                onClick={() => navigate('/superadmin')}
                                style={{ animationDelay: '0.45s' }}
                            >
                                <div className="ql-icon">
                                    <Shield size={24} />
                                </div>
                                <div className="ql-info">
                                    <h3>Super Admin Panel</h3>
                                    <p>Cross-campus stats, school management, create admin accounts</p>
                                </div>
                                <ChevronRight size={20} className="ql-arrow" />
                            </div>
                        )}
                    </div>

                    {/* Recent Actions */}
                    <h2 className="section-title">Recent Admin Actions</h2>
                    <div className="actions-card glass">
                        {mockAdminActions.map((action, i) => {
                            const config = actionIcons[action.type] || actionIcons.ITEM_REMOVAL;
                            const ActionIcon = config.icon;
                            return (
                                <div className="action-row" key={action.id} style={{ animationDelay: `${(i + 8) * 0.04}s` }}>
                                    <div className="action-icon" style={{ background: `${config.color}15`, color: config.color }}>
                                        <ActionIcon size={16} />
                                    </div>
                                    <div className="action-info">
                                        <span className="action-text">
                                            <strong>{action.action}</strong> — {action.target}
                                        </span>
                                        <span className="action-meta">
                                            by {action.admin} &middot; {timeAgo(action.timestamp)}
                                        </span>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
