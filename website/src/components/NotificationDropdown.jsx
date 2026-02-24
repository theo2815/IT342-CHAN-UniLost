import { useNavigate } from "react-router-dom";
import {
  Bell,
  CheckCircle,
  XCircle,
  Clock,
  Package,
  Search,
  AlertTriangle,
  Check,
} from "lucide-react";
import {
  getRecentNotifications,
  markAllAsRead,
  markAsRead,
  timeAgo,
} from "../mockData/notifications";
import "./NotificationDropdown.css";

const typeConfig = {
  CLAIM_RECEIVED: { icon: <Bell size={16} />, color: "#a855f7" },
  CLAIM_APPROVED: { icon: <CheckCircle size={16} />, color: "#22c55e" },
  CLAIM_REJECTED: { icon: <XCircle size={16} />, color: "#ef4444" },
  HANDOVER_CONFIRMED: { icon: <Check size={16} />, color: "#10b981" },
  HANDOVER_REMINDER: { icon: <Clock size={16} />, color: "#f59e0b" },
  ITEM_EXPIRED: { icon: <AlertTriangle size={16} />, color: "#94a3b8" },
  ITEM_MATCH: { icon: <Search size={16} />, color: "#3b82f6" },
};

function NotificationDropdown({ onClose, onCountChange }) {
  const navigate = useNavigate();
  const notifications = getRecentNotifications(5);

  const handleMarkAllRead = () => {
    markAllAsRead();
    if (onCountChange) onCountChange();
  };

  const handleClick = (notif) => {
    markAsRead(notif.id);
    if (onCountChange) onCountChange();
    onClose();
    navigate(notif.linkTo);
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
        {notifications.length > 0 ? (
          notifications.map((notif) => {
            const config = typeConfig[notif.type] || typeConfig.ITEM_MATCH;
            return (
              <div
                key={notif.id}
                className={`nd-item ${!notif.isRead ? "unread" : ""}`}
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
                {!notif.isRead && <span className="nd-unread-dot" />}
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
