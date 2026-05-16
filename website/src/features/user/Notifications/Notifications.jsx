import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, CheckCircle, XCircle, Clock, Search, AlertTriangle, Check, CheckCheck, MessageSquare, Loader, HandMetal, PackageCheck } from 'lucide-react';
import Header from '../../../shared/layout/Header/Header';
import EmptyState from '../../../shared/components/EmptyState/EmptyState';
import notificationService from '../notificationService';
import './Notifications.css';

const TABS = [
    { key: 'ALL', label: 'All' },
    { key: 'UNREAD', label: 'Unread' },
    { key: 'CLAIMS', label: 'Claims' },
    { key: 'ITEMS', label: 'Items' },
];

const claimTypes = ['CLAIM_RECEIVED', 'CLAIM_ACCEPTED', 'CLAIM_REJECTED', 'HANDOVER_CONFIRMED', 'HANDOVER_COMPLETE', 'HANDOVER_REMINDER', 'ITEM_MARKED_RETURNED', 'ITEM_RETURNED'];
const itemTypes = ['ITEM_EXPIRED', 'ITEM_MATCH', 'ITEM_FLAGGED'];

const typeConfig = {
    CLAIM_RECEIVED: { icon: <Bell size={18} />, color: '#a855f7', label: 'Claim Received' },
    CLAIM_ACCEPTED: { icon: <CheckCircle size={18} />, color: '#22c55e', label: 'Claim Accepted' },
    CLAIM_REJECTED: { icon: <XCircle size={18} />, color: '#ef4444', label: 'Claim Rejected' },
    HANDOVER_CONFIRMED: { icon: <Check size={18} />, color: '#10b981', label: 'Handover Confirmed' },
    HANDOVER_COMPLETE: { icon: <CheckCircle size={18} />, color: '#10b981', label: 'Handover Complete' },
    HANDOVER_REMINDER: { icon: <Clock size={18} />, color: '#f59e0b', label: 'Handover Reminder' },
    ITEM_MARKED_RETURNED: { icon: <HandMetal size={18} />, color: '#d97706', label: 'Item Returned' },
    ITEM_RETURNED: { icon: <PackageCheck size={18} />, color: '#059669', label: 'Return Confirmed' },
    ITEM_EXPIRED: { icon: <AlertTriangle size={18} />, color: '#94a3b8', label: 'Item Expired' },
    ITEM_MATCH: { icon: <Search size={18} />, color: '#3b82f6', label: 'Possible Match' },
    ITEM_FLAGGED: { icon: <AlertTriangle size={18} />, color: '#f59e0b', label: 'Item Flagged' },
    NEW_MESSAGE: { icon: <MessageSquare size={18} />, color: '#3b82f6', label: 'New Message' },
};

function getNotifRoute(notif) {
    switch (notif.type) {
        case 'CLAIM_RECEIVED':
        case 'CLAIM_ACCEPTED':
        case 'CLAIM_REJECTED':
            return `/claims/${notif.linkId}`;
        case 'HANDOVER_CONFIRMED':
        case 'HANDOVER_COMPLETE':
        case 'ITEM_MARKED_RETURNED':
        case 'ITEM_RETURNED':
        case 'NEW_MESSAGE':
            return `/messages?chatId=${notif.linkId}`;
        case 'ITEM_FLAGGED':
        case 'ITEM_EXPIRED':
        case 'ITEM_MATCH':
        case 'REPORT_DISMISSED':
            return `/items/${notif.linkId}`;
        case 'ITEM_REPORTED':
        case 'APPEAL_SUBMITTED':
            return '/admin/reports';
        default:
            return '/notifications';
    }
}

function timeAgo(dateStr) {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    if (diff < 0) return 'just now';
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return 'just now';
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    const days = Math.floor(hrs / 24);
    if (days < 7) return `${days}d ago`;
    const weeks = Math.floor(days / 7);
    return `${weeks}w ago`;
}

function Notifications() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('ALL');
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        loadNotifications();
    }, [page]);

    const loadNotifications = async () => {
        setLoading(true);
        const result = await notificationService.getNotifications(page, 30);
        if (result.success) {
            setNotifications(result.data.content || []);
            setTotalPages(result.data.totalPages || 0);
        }
        setLoading(false);
    };

    const filteredNotifications = useMemo(() => {
        switch (activeTab) {
            case 'UNREAD':
                return notifications.filter((n) => !n.read);
            case 'CLAIMS':
                return notifications.filter((n) => claimTypes.includes(n.type));
            case 'ITEMS':
                return notifications.filter((n) => itemTypes.includes(n.type) || n.type === 'NEW_MESSAGE');
            default:
                return notifications;
        }
    }, [activeTab, notifications]);

    const unreadCount = notifications.filter((n) => !n.read).length;

    const stats = useMemo(() => ({
        UNREAD: notifications.filter((n) => !n.read).length,
        CLAIMS: notifications.filter((n) => claimTypes.includes(n.type)).length,
        ITEMS: notifications.filter((n) => itemTypes.includes(n.type) || n.type === 'NEW_MESSAGE').length,
    }), [notifications]);

    const handleMarkAllRead = async () => {
        await notificationService.markAllAsRead();
        setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    };

    const handleClick = async (notif) => {
        if (!notif.read) {
            await notificationService.markAsRead(notif.id);
            setNotifications((prev) =>
                prev.map((n) => n.id === notif.id ? { ...n, read: true } : n)
            );
        }
        navigate(getNotifRoute(notif));
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
                    {loading ? (
                        <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
                            <Loader size={28} className="spin" />
                        </div>
                    ) : filteredNotifications.length > 0 ? (
                        <div className="notif-list">
                            {filteredNotifications.map((notif, index) => {
                                const config = typeConfig[notif.type] || typeConfig.ITEM_MATCH;
                                return (
                                    <div
                                        key={notif.id}
                                        className={`notif-card glass ${!notif.read ? 'unread' : ''}`}
                                        style={{ animationDelay: `${index * 0.04}s` }}
                                        onClick={() => handleClick(notif)}
                                    >
                                        {!notif.read && <div className="notif-unread-bar" />}
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
                                        {!notif.read && <span className="notif-dot" />}
                                    </div>
                                );
                            })}

                            {/* Pagination */}
                            {totalPages > 1 && (
                                <div className="notif-pagination">
                                    <button
                                        className="btn btn-outline"
                                        disabled={page === 0}
                                        onClick={() => setPage((p) => Math.max(0, p - 1))}
                                    >
                                        Previous
                                    </button>
                                    <span className="notif-page-info">Page {page + 1} of {totalPages}</span>
                                    <button
                                        className="btn btn-outline"
                                        disabled={page >= totalPages - 1}
                                        onClick={() => setPage((p) => p + 1)}
                                    >
                                        Next
                                    </button>
                                </div>
                            )}
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
