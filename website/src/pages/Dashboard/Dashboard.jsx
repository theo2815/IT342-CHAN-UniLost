import { useState } from 'react';
import Header from '../../components/Header';
import './Dashboard.css';

function Dashboard() {
    const [activeNav, setActiveNav] = useState('dashboard');

    const stats = [
        { id: 1, label: 'Total Users', value: '2,543', icon: '👥', trend: '+12%', color: 'blue' },
        { id: 2, label: 'Revenue', value: '$45,678', icon: '💰', trend: '+8%', color: 'green' },
        { id: 3, label: 'Orders', value: '1,234', icon: '📦', trend: '+23%', color: 'purple' },
        { id: 4, label: 'Visitors', value: '8,901', icon: '📊', trend: '+5%', color: 'orange' },
    ];

    const recentActivity = [
        { id: 1, action: 'New user registered', user: 'John Doe', time: '2 minutes ago', icon: '👤' },
        { id: 2, action: 'Order completed', user: 'Jane Smith', time: '15 minutes ago', icon: '✅' },
        { id: 3, action: 'Payment received', user: 'Bob Wilson', time: '1 hour ago', icon: '💳' },
        { id: 4, action: 'New comment', user: 'Alice Brown', time: '2 hours ago', icon: '💬' },
    ];

    return (
        <div className="dashboard-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <div className="page-header">
                        <h1>Dashboard</h1>
                        <p>Welcome to your control center.</p>
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
                        {/* Chart Placeholder */}
                        <div className="chart-card glass">
                            <div className="card-header">
                                <h3>Revenue Overview</h3>
                                <select className="period-select">
                                    <option>Last 7 days</option>
                                    <option>Last 30 days</option>
                                    <option>Last 90 days</option>
                                </select>
                            </div>
                            <div className="chart-placeholder">
                                <div className="chart-bars">
                                    {[40, 65, 45, 80, 55, 90, 70].map((height, i) => (
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
