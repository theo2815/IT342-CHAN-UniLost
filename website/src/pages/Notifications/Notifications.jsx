import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, CheckCircle, XCircle, Clock, Search, AlertTriangle, Check, CheckCheck } from 'lucide-react';
import Header from '../../components/Header';
import EmptyState from '../../components/EmptyState';
import { mockNotifications, markAllAsRead, markAsRead, getUnreadCount, timeAgo } from '../../mockData/notifications';
import './Notifications.css';

const TABS = [
    { key: 'ALL', label: 'All' },
    { key: 'UNREAD', label: 'Unread' },
    { key: 'CLAIMS', label: 'Claims' },
    { key: 'ITEMS', label: 'Items' },
];

const claimTypes = ['CLAIM_RECEIVED', 'CLAIM_APPROVED', 'CLAIM_REJECTED', 'HANDOVER_CONFIRMED', 'HANDOVER_REMINDER'];
const itemTypes = ['ITEM_EXPIRED', 'ITEM_MATCH'];

const typeConfig = {
    CLAIM_RECEIVED: { icon: <Bell size={18} />, color: '#a855f7', label: 'Claim Received' },
    CLAIM_APPROVED: { icon: <CheckCircle size={18} />, color: '#22c55e', label: 'Claim Approved' },
    CLAIM_REJECTED: { icon: <XCircle size={18} />, color: '#ef4444', label: 'Claim Rejected' },
    HANDOVER_CONFIRMED: { icon: <Check size={18} />, color: '#10b981', label: 'Handover Complete' },
    HANDOVER_REMINDER: { icon: <Clock size={18} />, color: '#f59e0b', label: 'Handover Reminder' },
    ITEM_EXPIRED: { icon: <AlertTriangle size={18} />, color: '#94a3b8', label: 'Item Expired' },
    ITEM_MATCH: { icon: <Search size={18} />, color: '#3b82f6', label: 'Possible Match' },
};

function Notifications() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('ALL');
    const [, forceUpdate] = useState(0);

    const filteredNotifications = useMemo(() => {
        switch (activeTab) {
            case 'UNREAD':
                return mockNotifications.filter((n) => !n.isRead);
            case 'CLAIMS':
                return mockNotifications.filter((n) => claimTypes.includes(n.type));
            case 'ITEMS':
                return mockNotifications.filter((n) => itemTypes.includes(n.type));
            default:
                return mockNotifications;
        }
    }, [activeTab, forceUpdate]); // eslint-disable-line react-hooks/exhaustive-deps

    const unreadCount = getUnreadCount();

    const stats = useMemo(() => ({
        UNREAD: mockNotifications.filter((n) => !n.isRead).length,
        CLAIMS: mockNotifications.filter((n) => claimTypes.includes(n.type)).length,
        ITEMS: mockNotifications.filter((n) => itemTypes.includes(n.type)).length,
    }), [forceUpdate]); // eslint-disable-line react-hooks/exhaustive-deps

    const handleMarkAllRead = () => {
        markAllAsRead();
        forceUpdate((v) => v + 1);
    };

    const handleClick = (notif) => {
        markAsRead(notif.id);
        forceUpdate((v) => v + 1);
        navigate(notif.linkTo);
    };

    return (
        <div className="notifications-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <div className="page-header">
                        <div className="page-header-text">
                            <div className="page-title-row">
                                <h1>Notifications</h1>
                                {unreadCount > 0 && (
                                    <span className="unread-count-badge">{unreadCount} unread</span>
                                )}
                            </div>
                            <p>Stay updated on your items and claims.</p>
                        </div>
                        {unreadCount > 0 && (
                            <button className="mark-all-btn" onClick={handleMarkAllRead}>
                                <CheckCheck size={16} />
                                Mark all as read
                            </button>
                        )}
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

                    {/* Notification List */}
                    {filteredNotifications.length > 0 ? (
                        <div className="notif-list">
                            {filteredNotifications.map((notif, index) => {
                                const config = typeConfig[notif.type] || typeConfig.ITEM_MATCH;
                                return (
                                    <div
                                        key={notif.id}
                                        className={`notif-card glass ${!notif.isRead ? 'unread' : ''}`}
                                        style={{ animationDelay: `${index * 0.04}s` }}
                                        onClick={() => handleClick(notif)}
                                    >
                                        {!notif.isRead && <div className="notif-unread-bar" />}
                                        <div
                                            className="notif-icon"
                                            style={{ background: `${config.color}15`, color: config.color }}
                                        >
                                            {config.icon}
                                        </div>
                                        <div className="notif-content">
                                            <div className="notif-top">
                                                <h3 className="notif-title">{notif.title}</h3>
                                                <span className="notif-type-label" style={{ color: config.color }}>
                                                    {config.label}
                                                </span>
                                            </div>
                                            <p className="notif-message">{notif.message}</p>
                                            <span className="notif-time">{timeAgo(notif.createdAt)}</span>
                                        </div>
                                        {!notif.isRead && <span className="notif-dot" />}
                                    </div>
                                );
                            })}
                        </div>
                    ) : (
                        <EmptyState
                            title={activeTab === 'ALL' ? 'No notifications yet' : `No ${activeTab.toLowerCase()} notifications`}
                            message="When there's activity on your items or claims, you'll see it here."
                        />
                    )}
                </div>
            </main>
        </div>
    );
}

export default Notifications;
