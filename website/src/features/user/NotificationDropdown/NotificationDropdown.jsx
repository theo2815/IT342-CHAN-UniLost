import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Bell,
  CheckCircle,
  XCircle,
  Clock,
  Search,
  AlertTriangle,
  Check,
  MessageSquare,
  Loader,
  HandMetal,
  PackageCheck,
} from "lucide-react";
import notificationService from "../services/notificationService";
import "./NotificationDropdown.css";

const typeConfig = {
  CLAIM_RECEIVED: { icon: <Bell size={16} />, color: "#a855f7" },
  CLAIM_ACCEPTED: { icon: <CheckCircle size={16} />, color: "#22c55e" },
  CLAIM_REJECTED: { icon: <XCircle size={16} />, color: "#ef4444" },
  HANDOVER_CONFIRMED: { icon: <Check size={16} />, color: "#10b981" },
  HANDOVER_COMPLETE: { icon: <CheckCircle size={16} />, color: "#10b981" },
  HANDOVER_REMINDER: { icon: <Clock size={16} />, color: "#f59e0b" },
  ITEM_MARKED_RETURNED: { icon: <HandMetal size={16} />, color: "#d97706" },
  ITEM_RETURNED: { icon: <PackageCheck size={16} />, color: "#059669" },
  ITEM_EXPIRED: { icon: <AlertTriangle size={16} />, color: "#94a3b8" },
  ITEM_MATCH: { icon: <Search size={16} />, color: "#3b82f6" },
  ITEM_FLAGGED: { icon: <AlertTriangle size={16} />, color: "#f59e0b" },
  ITEM_FLAG_THRESHOLD: { icon: <AlertTriangle size={16} />, color: "#ef4444" },
  NEW_MESSAGE: { icon: <MessageSquare size={16} />, color: "#3b82f6" },
};

function getNotifRoute(notif) {
  switch (notif.type) {
    case "CLAIM_RECEIVED":
    case "CLAIM_ACCEPTED":
    case "CLAIM_REJECTED":
      return `/claims/${notif.linkId}`;
    case "HANDOVER_CONFIRMED":
    case "HANDOVER_COMPLETE":
    case "ITEM_MARKED_RETURNED":
    case "ITEM_RETURNED":
    case "NEW_MESSAGE":
      return `/messages?chatId=${notif.linkId}`;
    case "ITEM_FLAG_THRESHOLD":
      return `/admin/items`;
    case "ITEM_FLAGGED":
    case "ITEM_EXPIRED":
    case "ITEM_MATCH":
      return `/items/${notif.linkId}`;
    default:
      return "/notifications";
  }
}

function timeAgo(dateStr) {
  if (!dateStr) return "";
  const diff = Date.now() - new Date(dateStr).getTime();
  if (diff < 0) return "just now";
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days < 7) return `${days}d ago`;
  const weeks = Math.floor(days / 7);
  return `${weeks}w ago`;
}

function NotificationDropdown({ onClose, onCountChange }) {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      const result = await notificationService.getNotifications(0, 5);
      if (result.success) {
        setNotifications(result.data.content || []);
      }
      setLoading(false);
    };
    load();
  }, []);

  const handleMarkAllRead = async () => {
    await notificationService.markAllAsRead();
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    if (onCountChange) onCountChange();
  };

  const handleClick = async (notif) => {
    if (!notif.read) {
      await notificationService.markAsRead(notif.id);
      if (onCountChange) onCountChange();
    }
    onClose();
    navigate(getNotifRoute(notif));
  };

  return (
    <div className="notification-dropdown glass">
      <div className="nd-header">
        <h3>Notifications</h3>
        <button className="nd-mark-all" onClick={handleMarkAllRead}>
          Mark all as read
        </button>
      </div>

      <div className="nd-list">
        {loading ? (
          <div className="nd-empty">
            <Loader size={20} className="spin" />
          </div>
        ) : notifications.length > 0 ? (
          notifications.map((notif) => {
            const config = typeConfig[notif.type] || typeConfig.ITEM_MATCH;
            return (
              <div
                key={notif.id}
                className={`nd-item ${!notif.read ? "unread" : ""}`}
                onClick={() => handleClick(notif)}
              >
                <div
                  className="nd-item-icon"
                  style={{
                    background: `${config.color}15`,
                    color: config.color,
                  }}
                >
                  {config.icon}
                </div>
                <div className="nd-item-content">
                  <p className="nd-item-title">{notif.title}</p>
                  <p className="nd-item-message">{notif.message}</p>
                  <span className="nd-item-time">
                    {timeAgo(notif.createdAt)}
                  </span>
                </div>
                {!notif.read && <span className="nd-unread-dot" />}
              </div>
            );
          })
        ) : (
          <div className="nd-empty">
            <Bell size={24} />
            <p>No notifications</p>
          </div>
        )}
      </div>

      <div className="nd-footer">
        <button
          className="nd-view-all"
          onClick={() => {
            onClose();
            navigate("/notifications");
          }}
        >
          View all notifications
        </button>
      </div>
    </div>
  );
}

export default NotificationDropdown;
