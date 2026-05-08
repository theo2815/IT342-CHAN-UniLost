import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { Activity, Database, Clock, Cpu, Loader2, AlertCircle } from 'lucide-react';
import Header from '../../shared/layout/Header/Header';
import adminService from './adminService';
import './AdminHealth.css';

const AdminHealth = () => {
    const [health, setHealth] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const intervalRef = useRef(null);

    const fetchHealth = async () => {
        const result = await adminService.getSystemHealth();
        if (result.success) {
            setHealth(result.data);
            setError('');
        } else {
            setError(result.error);
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchHealth();
        intervalRef.current = setInterval(fetchHealth, 30000);
        return () => clearInterval(intervalRef.current);
    }, []);

    const getMemoryLevel = (percent) => {
        if (percent < 60) return 'low';
        if (percent < 85) return 'medium';
        return 'high';
    };

    return (
        <div className="admin-health-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>System Health</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <Activity size={24} />
                            <h1>System Health</h1>
                            <span className="auto-refresh-badge">Auto-refresh 30s</span>
                        </div>
                    </div>

                    {loading && (
                        <div className="loading-state">
                            <Loader2 size={32} className="spinner" />
                            <p>Loading health data...</p>
                        </div>
                    )}

                    {error && !loading && (
                        <div className="error-state">
                            <AlertCircle size={24} />
                            <p>{error}</p>
                        </div>
                    )}

                    {!loading && health && (
                        <>
                            {/* Health Cards */}
                            <div className="health-grid">
                                {/* MongoDB Status */}
                                <div className="health-card glass" style={{ animationDelay: '0s' }}>
                                    <h3><Database size={16} /> MongoDB</h3>
                                    <div className="status-indicator">
                                        <span className={`status-dot ${health.mongoStatus === 'CONNECTED' ? 'connected' : 'disconnected'}`} />
                                        <span>{health.mongoStatus === 'CONNECTED' ? 'Connected' : 'Disconnected'}</span>
                                    </div>
                                </div>

                                {/* Uptime */}
                                <div className="health-card glass" style={{ animationDelay: '0.05s' }}>
                                    <h3><Clock size={16} /> Uptime</h3>
                                    <span className="health-value">{health.uptimeFormatted}</span>
                                </div>

                                {/* Memory Usage */}
                                <div className="health-card glass" style={{ animationDelay: '0.1s' }}>
                                    <h3><Cpu size={16} /> Memory Usage</h3>
                                    <span className="health-value">{health.memory?.usagePercent}%</span>
                                    <div className="memory-bar-container">
                                        <div className="memory-bar">
                                            <div
                                                className={`memory-bar-fill ${getMemoryLevel(health.memory?.usagePercent)}`}
                                                style={{ width: `${health.memory?.usagePercent}%` }}
                                            />
                                        </div>
                                        <div className="memory-text">
                                            <span>{health.memory?.usedMb} MB used</span>
                                            <span>{health.memory?.maxMb} MB max</span>
                                        </div>
                                    </div>
                                </div>

                                {/* Java Version */}
                                <div className="health-card glass" style={{ animationDelay: '0.15s' }}>
                                    <h3>Java Version</h3>
                                    <span className="health-value small">{health.javaVersion}</span>
                                </div>
                            </div>

                            {/* Collection Counts */}
                            <h2 className="section-title">Database Collections</h2>
                            <div className="collection-grid">
                                {health.collectionCounts && Object.entries(health.collectionCounts).map(([name, count], i) => (
                                    <div className="collection-card glass" key={name} style={{ animationDelay: `${(i + 4) * 0.05}s` }}>
                                        <span className="cc-count">{count.toLocaleString()}</span>
                                        <span className="cc-name">{name}</span>
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

export default AdminHealth;
