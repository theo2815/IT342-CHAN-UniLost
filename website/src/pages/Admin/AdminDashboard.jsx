import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Users, Package, Clock, CheckCircle, Ban, ChevronRight, Loader2, AlertCircle, ScrollText, BarChart3, Activity } from 'lucide-react';
import Header from '../../components/Header';
import adminService from '../../services/adminService';
import './AdminDashboard.css';

const AdminDashboard = () => {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchStats = async () => {
            setLoading(true);
            const result = await adminService.getDashboardStats();
            if (result.success) {
                setStats(result.data);
            } else {
                setError(result.error);
            }
            setLoading(false);
        };
        fetchStats();
    }, []);

    const statCards = stats ? [
        { label: 'Total Users', value: stats.totalUsers, icon: Users, color: '#3b82f6' },
        { label: 'Active Items', value: stats.activeItems, icon: Package, color: '#10b981' },
        { label: 'Pending Claims', value: stats.pendingClaims, icon: Clock, color: '#f59e0b' },
        { label: 'Recovered This Month', value: stats.recoveredThisMonth, icon: CheckCircle, color: '#059669' },
        { label: 'Suspended Users', value: stats.suspendedUsers, icon: Ban, color: '#ef4444' },
    ] : [];

    const quickLinks = [
        { label: 'Items Management', description: 'View, filter, and remove item listings', route: '/admin/items', icon: Package },
        { label: 'Users Management', description: 'Search, ban, and manage user accounts', route: '/admin/users', icon: Users },
        { label: 'Claims Management', description: 'Oversee claims and override handovers', route: '/admin/claims', icon: Shield },
        { label: 'Campus Management', description: 'Cross-campus stats and school settings', route: '/admin/campuses', icon: Shield },
        { label: 'Audit Logs', description: 'Track admin actions and changes', route: '/admin/audit-logs', icon: ScrollText },
        { label: 'Analytics & Reports', description: 'Charts, trends, and campus comparisons', route: '/admin/analytics', icon: BarChart3 },
        { label: 'System Health', description: 'Monitor database, memory, and uptime', route: '/admin/health', icon: Activity },
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
                            <span className="role-badge">Admin</span>
                        </div>
                        <p>Manage items, users, and claims across all campuses</p>
                    </div>

                    {loading && (
                        <div className="loading-state">
                            <Loader2 size={32} className="spinner" />
                            <p>Loading dashboard...</p>
                        </div>
                    )}

                    {error && (
                        <div className="error-state">
                            <AlertCircle size={24} />
                            <p>{error}</p>
                        </div>
                    )}

                    {!loading && !error && stats && (
                        <>
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

                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
