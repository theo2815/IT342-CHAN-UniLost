import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Edit3, Eye, XCircle, Package, CheckCircle, Clock, Archive, Ban } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import { mockItems, timeAgo } from '../../mockData/items';
import './MyItems.css';

const TABS = [
    { key: 'ALL', label: 'All' },
    { key: 'ACTIVE', label: 'Active' },
    { key: 'CLAIMED', label: 'Claimed' },
    { key: 'HANDED_OVER', label: 'Handed Over' },
    { key: 'EXPIRED', label: 'Expired' },
    { key: 'CANCELLED', label: 'Cancelled' },
];

function MyItems() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('ALL');

    // Mock: filter items posted by user u1 (our mock current user)
    const myItems = useMemo(() => {
        return mockItems.filter((item) => item.postedBy?.id === 'u1');
    }, []);

    const filteredItems = useMemo(() => {
        if (activeTab === 'ALL') return myItems;
        return myItems.filter((item) => item.status === activeTab);
    }, [myItems, activeTab]);

    // Quick stats
    const stats = useMemo(() => ({
        ACTIVE: myItems.filter((i) => i.status === 'ACTIVE').length,
        CLAIMED: myItems.filter((i) => i.status === 'CLAIMED').length,
        HANDED_OVER: myItems.filter((i) => i.status === 'HANDED_OVER').length,
        EXPIRED: myItems.filter((i) => i.status === 'EXPIRED').length,
        CANCELLED: myItems.filter((i) => i.status === 'CANCELLED').length,
    }), [myItems]);

    const statCards = [
        { key: 'ACTIVE', label: 'Active', value: stats.ACTIVE, icon: <Package size={20} />, color: 'blue' },
        { key: 'CLAIMED', label: 'Claimed', value: stats.CLAIMED, icon: <Clock size={20} />, color: 'purple' },
        { key: 'HANDED_OVER', label: 'Recovered', value: stats.HANDED_OVER, icon: <CheckCircle size={20} />, color: 'green' },
        { key: 'EXPIRED', label: 'Expired', value: stats.EXPIRED, icon: <Archive size={20} />, color: 'gray' },
        { key: 'CANCELLED', label: 'Cancelled', value: stats.CANCELLED, icon: <Ban size={20} />, color: 'gray' },
    ];

    return (
        <div className="my-items-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <div className="page-header">
                        <div className="page-header-text">
                            <h1>My Items</h1>
                            <p>Manage your lost and found reports.</p>
                        </div>
                        <button
                            className="post-item-btn"
                            onClick={() => navigate('/post-item')}
                        >
                            <Plus size={18} />
                            Post New Item
                        </button>
                    </div>

                    {/* Quick Stats */}
                    <div className="quick-stats">
                        {statCards.map((stat) => (
                            <div
                                key={stat.key}
                                className={`quick-stat-card glass stat-${stat.color}`}
                                onClick={() => setActiveTab(stat.key)}
                            >
                                <span className="qs-icon">{stat.icon}</span>
                                <span className="qs-value">{stat.value}</span>
                                <span className="qs-label">{stat.label}</span>
                            </div>
                        ))}
                    </div>

                    {/* Tabs */}
                    <div className="tabs">
                        {TABS.map((tab) => (
                            <button
                                key={tab.key}
                                className={`tab ${activeTab === tab.key ? 'active' : ''}`}
                                onClick={() => setActiveTab(tab.key)}
                            >
                                {tab.label}
                                {tab.key !== 'ALL' && stats[tab.key] > 0 && (
                                    <span className="tab-count">{stats[tab.key]}</span>
                                )}
                            </button>
                        ))}
                    </div>

                    {/* Items List */}
                    {filteredItems.length > 0 ? (
                        <div className="my-items-list">
                            {filteredItems.map((item, index) => (
                                <div
                                    key={item.id}
                                    className="my-item-row glass"
                                    style={{ animationDelay: `${index * 0.05}s` }}
                                >
                                    <div className="my-item-image" onClick={() => navigate(`/items/${item.id}`)}>
                                        <img src={item.imageUrl} alt={item.title} />
                                        <span className={`type-badge ${item.type.toLowerCase()}`}>
                                            {item.type}
                                        </span>
                                    </div>
                                    <div className="my-item-info" onClick={() => navigate(`/items/${item.id}`)}>
                                        <div className="my-item-top">
                                            <h3>{item.title}</h3>
                                            <StatusBadge status={item.status} />
                                        </div>
                                        <p className="my-item-desc">{item.description}</p>
                                        <div className="my-item-meta">
                                            <span>{item.category}</span>
                                            <span>&middot;</span>
                                            <span>{item.locationDescription}</span>
                                            <span>&middot;</span>
                                            <span>{timeAgo(item.createdAt)}</span>
                                        </div>
                                    </div>
                                    <div className="my-item-actions">
                                        <button className="mi-action-btn" title="View Details" onClick={() => navigate(`/items/${item.id}`)}>
                                            <Eye size={16} />
                                        </button>
                                        {item.status === 'ACTIVE' && (
                                            <>
                                                <button className="mi-action-btn" title="Edit">
                                                    <Edit3 size={16} />
                                                </button>
                                                <button className="mi-action-btn danger" title="Cancel">
                                                    <XCircle size={16} />
                                                </button>
                                            </>
                                        )}
                                        {item.claimCount > 0 && (
                                            <button className="mi-claims-btn" title="View Claims" onClick={() => navigate(`/my-items/${item.id}/claims`)}>
                                                Claims
                                                <span className="claims-badge">{item.claimCount}</span>
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <EmptyState
                            title={activeTab === 'ALL' ? 'No items yet' : `No ${activeTab.toLowerCase().replace('_', ' ')} items`}
                            message="Post a lost or found item to get started."
                            actionLabel="Post an Item"
                            onAction={() => navigate('/post-item')}
                        />
                    )}
                </div>
            </main>
        </div>
    );
}

export default MyItems;
