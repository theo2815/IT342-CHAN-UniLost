import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { BarChart3, Package, TrendingUp, Search as SearchIcon, MapPin, Loader2, AlertCircle } from 'lucide-react';
import {
    PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
    BarChart, Bar, XAxis, YAxis, CartesianGrid,
    AreaChart, Area
} from 'recharts';
import Header from '../../components/Header';
import adminService from '../../services/adminService';
import './AdminAnalytics.css';

const STATUS_COLORS = {
    ACTIVE: '#3b82f6',
    CLAIMED: '#f59e0b',
    PENDING_OWNER_CONFIRMATION: '#8b5cf6',
    RETURNED: '#10b981',
    EXPIRED: '#94a3b8',
    TURNED_OVER_TO_OFFICE: '#06b6d4',
    HIDDEN: '#ef4444',
    HANDED_OVER: '#059669',
};

const CHART_COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899', '#f97316'];

const AdminAnalytics = () => {
    const [analytics, setAnalytics] = useState(null);
    const [campusStats, setCampusStats] = useState([]);
    const [itemTrends, setItemTrends] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchAll = async () => {
            setLoading(true);
            setError('');

            const [analyticsRes, campusRes, trendsRes] = await Promise.all([
                adminService.getAnalytics(),
                adminService.getCrossCampusStats(),
                adminService.getItemTrends(12),
            ]);

            if (analyticsRes.success) setAnalytics(analyticsRes.data);
            else setError(analyticsRes.error);

            if (campusRes.success) setCampusStats(campusRes.data || []);
            if (trendsRes.success) setItemTrends(trendsRes.data || []);

            setLoading(false);
        };
        fetchAll();
    }, []);

    // Transform data for charts
    const statusData = analytics?.statusCounts
        ? Object.entries(analytics.statusCounts).map(([name, value]) => ({
            name: name.replace(/_/g, ' '),
            value,
            fill: STATUS_COLORS[name] || '#94a3b8',
        }))
        : [];

    const typeData = analytics
        ? [
            { name: 'Lost', count: analytics.lostCount || 0, fill: '#ef4444' },
            { name: 'Found', count: analytics.foundCount || 0, fill: '#10b981' },
        ]
        : [];

    const categoryData = (analytics?.topCategories || []).map((c, i) => ({
        name: c.category,
        count: c.count,
        fill: CHART_COLORS[i % CHART_COLORS.length],
    }));

    const locationData = (analytics?.topLocations || []).map((l, i) => ({
        name: l.location?.length > 25 ? l.location.substring(0, 25) + '...' : l.location,
        count: l.count,
        fill: CHART_COLORS[i % CHART_COLORS.length],
    }));

    const campusChartData = campusStats
        .filter(c => c.itemCount > 0 || c.userCount > 0)
        .map(c => ({
            name: c.shortLabel || c.universityCode,
            users: c.userCount,
            items: c.itemCount,
            recovery: c.recoveryRate,
        }));

    const summaryCards = analytics ? [
        { label: 'Total Items', value: analytics.totalItems, color: '#3b82f6', icon: Package },
        { label: 'Recovery Rate', value: `${analytics.recoveryRate}%`, color: '#10b981', icon: TrendingUp },
        { label: 'Lost Items', value: analytics.lostCount, color: '#ef4444', icon: SearchIcon },
        { label: 'Found Items', value: analytics.foundCount, color: '#059669', icon: Package },
        { label: 'Resolved', value: analytics.resolvedCount, color: '#8b5cf6', icon: MapPin },
    ] : [];

    return (
        <div className="admin-analytics-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>Analytics</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <BarChart3 size={24} />
                            <h1>Analytics & Reports</h1>
                        </div>
                    </div>

                    {loading && (
                        <div className="loading-state">
                            <Loader2 size={32} className="spinner" />
                            <p>Loading analytics...</p>
                        </div>
                    )}

                    {error && (
                        <div className="error-state">
                            <AlertCircle size={24} />
                            <p>{error}</p>
                        </div>
                    )}

                    {!loading && !error && analytics && (
                        <>
                            {/* Summary Cards */}
                            <div className="analytics-summary">
                                {summaryCards.map((card, i) => (
                                    <div className="summary-card glass" key={i} style={{ animationDelay: `${i * 0.05}s` }}>
                                        <div className="summary-icon" style={{ background: `${card.color}15`, color: card.color }}>
                                            <card.icon size={22} />
                                        </div>
                                        <span className="summary-value" style={{ color: card.color }}>{card.value}</span>
                                        <span className="summary-label">{card.label}</span>
                                    </div>
                                ))}
                            </div>

                            {/* Charts */}
                            <div className="charts-grid">
                                {/* Status Distribution */}
                                <div className="chart-card glass" style={{ animationDelay: '0.1s' }}>
                                    <h3>Item Status Distribution</h3>
                                    <div className="chart-container">
                                        <ResponsiveContainer width="100%" height="100%">
                                            <PieChart>
                                                <Pie
                                                    data={statusData}
                                                    cx="50%"
                                                    cy="50%"
                                                    innerRadius={60}
                                                    outerRadius={100}
                                                    paddingAngle={2}
                                                    dataKey="value"
                                                >
                                                    {statusData.map((entry, i) => (
                                                        <Cell key={i} fill={entry.fill} />
                                                    ))}
                                                </Pie>
                                                <Tooltip />
                                                <Legend />
                                            </PieChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>

                                {/* Lost vs Found */}
                                <div className="chart-card glass" style={{ animationDelay: '0.15s' }}>
                                    <h3>Lost vs Found</h3>
                                    <div className="chart-container">
                                        <ResponsiveContainer width="100%" height="100%">
                                            <BarChart data={typeData}>
                                                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                                                <XAxis dataKey="name" />
                                                <YAxis />
                                                <Tooltip />
                                                <Bar dataKey="count" radius={[8, 8, 0, 0]}>
                                                    {typeData.map((entry, i) => (
                                                        <Cell key={i} fill={entry.fill} />
                                                    ))}
                                                </Bar>
                                            </BarChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>

                                {/* Top Categories */}
                                <div className="chart-card glass" style={{ animationDelay: '0.2s' }}>
                                    <h3>Top Categories</h3>
                                    <div className="chart-container">
                                        <ResponsiveContainer width="100%" height="100%">
                                            <BarChart data={categoryData} layout="vertical">
                                                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                                                <XAxis type="number" />
                                                <YAxis dataKey="name" type="category" width={100} tick={{ fontSize: 12 }} />
                                                <Tooltip />
                                                <Bar dataKey="count" radius={[0, 8, 8, 0]}>
                                                    {categoryData.map((entry, i) => (
                                                        <Cell key={i} fill={entry.fill} />
                                                    ))}
                                                </Bar>
                                            </BarChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>

                                {/* Top Locations */}
                                <div className="chart-card glass" style={{ animationDelay: '0.25s' }}>
                                    <h3>Top Locations</h3>
                                    <div className="chart-container">
                                        <ResponsiveContainer width="100%" height="100%">
                                            <BarChart data={locationData} layout="vertical">
                                                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                                                <XAxis type="number" />
                                                <YAxis dataKey="name" type="category" width={120} tick={{ fontSize: 11 }} />
                                                <Tooltip />
                                                <Bar dataKey="count" radius={[0, 8, 8, 0]}>
                                                    {locationData.map((entry, i) => (
                                                        <Cell key={i} fill={entry.fill} />
                                                    ))}
                                                </Bar>
                                            </BarChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>

                                {/* Items Over Time */}
                                {itemTrends.length > 0 && (
                                    <div className="chart-card glass full-width" style={{ animationDelay: '0.3s' }}>
                                        <h3>Items Over Time (Last 12 Months)</h3>
                                        <div className="chart-container tall">
                                            <ResponsiveContainer width="100%" height="100%">
                                                <AreaChart data={itemTrends}>
                                                    <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                                                    <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                                                    <YAxis />
                                                    <Tooltip />
                                                    <Area
                                                        type="monotone"
                                                        dataKey="count"
                                                        stroke="#3b82f6"
                                                        fill="rgba(59, 130, 246, 0.15)"
                                                        strokeWidth={2}
                                                    />
                                                </AreaChart>
                                            </ResponsiveContainer>
                                        </div>
                                    </div>
                                )}

                                {/* Campus Comparison */}
                                {campusChartData.length > 0 && (
                                    <div className="chart-card glass full-width" style={{ animationDelay: '0.35s' }}>
                                        <h3>Campus Comparison</h3>
                                        <div className="chart-container tall">
                                            <ResponsiveContainer width="100%" height="100%">
                                                <BarChart data={campusChartData}>
                                                    <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                                                    <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                                                    <YAxis />
                                                    <Tooltip />
                                                    <Legend />
                                                    <Bar dataKey="users" name="Users" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                                                    <Bar dataKey="items" name="Items" fill="#10b981" radius={[4, 4, 0, 0]} />
                                                </BarChart>
                                            </ResponsiveContainer>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminAnalytics;
