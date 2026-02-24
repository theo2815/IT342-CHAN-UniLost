import { useState, useEffect } from 'react';
import { Search, Package, CheckCircle, Clock, AlertTriangle } from 'lucide-react';
import Header from '../../components/Header';
import authService from '../../services/authService';
import './Dashboard.css';

function Dashboard() {
    const [user, setUser] = useState(null);

    useEffect(() => {
        const currentUser = authService.getCurrentUser();
        if (currentUser) {
            setUser(currentUser);
        }
    }, []);

    const stats = [
        { id: 1, label: 'Items Reported', value: '0', icon: <Package size={24} />, trend: '--', color: 'blue' },
        { id: 2, label: 'Items Recovered', value: '0', icon: <CheckCircle size={24} />, trend: '--', color: 'green' },
        { id: 3, label: 'Pending Claims', value: '0', icon: <Clock size={24} />, trend: '--', color: 'purple' },
        { id: 4, label: 'Active Listings', value: '0', icon: <Search size={24} />, trend: '--', color: 'orange' },
    ];

    const recentActivity = [
        { id: 1, action: 'Welcome to UniLost!', user: 'Get started by reporting a lost or found item.', time: 'Just now', icon: <Search size={18} /> },
    ];

    return (
        <div className="dashboard-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <div className="page-header">
                        <h1>Dashboard</h1>
                        <p>Welcome back{user ? `, ${user.firstName}` : ''}. Track lost & found items across Cebu City campuses.</p>
                    </div>

                    {/* Stats Grid */}
                    <section className="stats-grid">
                        {stats.map((stat, index) => (
                            <div
                                key={stat.id}
                                className={`stat-card glass stat-${stat.color}`}
                                style={{ animationDelay: `${index * 0.1}s` }}
                            >
                                <div className="stat-icon">{stat.icon}</div>
                                <div className="stat-info">
                                    <span className="stat-label">{stat.label}</span>
                                    <span className="stat-value">{stat.value}</span>
                                </div>
                                <span className="stat-trend">{stat.trend}</span>
                            </div>
                        ))}
                    </section>

                    {/* Content Grid */}
                    <section className="content-grid">
                        {/* Activity Overview */}
                        <div className="chart-card glass">
                            <div className="card-header">
                                <h3>Item Activity</h3>
                                <select className="period-select">
                                    <option>Last 7 days</option>
                                    <option>Last 30 days</option>
                                    <option>Last 90 days</option>
                                </select>
                            </div>
                            <div className="chart-placeholder">
                                <div className="chart-bars">
                                    {[10, 25, 15, 40, 20, 35, 30].map((height, i) => (
                                        <div
                                            key={i}
                                            className="chart-bar"
                                            style={{ '--height': `${height}%`, animationDelay: `${i * 0.1}s` }}
                                        ></div>
                                    ))}
                                </div>
                                <div className="chart-labels">
                                    <span>Mon</span>
                                    <span>Tue</span>
                                    <span>Wed</span>
                                    <span>Thu</span>
                                    <span>Fri</span>
                                    <span>Sat</span>
                                    <span>Sun</span>
                                </div>
                            </div>
                        </div>

                        {/* Recent Activity */}
                        <div className="activity-card glass">
                            <div className="card-header">
                                <h3>Recent Activity</h3>
                                <button className="view-all-btn">View All</button>
                            </div>
                            <ul className="activity-list">
                                {recentActivity.map((activity, index) => (
                                    <li
                                        key={activity.id}
                                        className="activity-item"
                                        style={{ animationDelay: `${index * 0.1}s` }}
                                    >
                                        <span className="activity-icon">{activity.icon}</span>
                                        <div className="activity-info">
                                            <span className="activity-action">{activity.action}</span>
                                            <span className="activity-user">{activity.user}</span>
                                        </div>
                                        <span className="activity-time">{activity.time}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </section>
                </div>
            </main>
        </div>
    );
}

export default Dashboard;
