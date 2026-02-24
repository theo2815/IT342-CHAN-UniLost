import React, { useState } from "react";
import {
  Search,
  CheckCircle,
  Clock,
  Shield,
  MoreVertical,
  PlusCircle,
  Image as ImageIcon,
  Send,
  MapPin,
  EyeOff,
  Droplets,
  Check,
  Bell,
  UserCircle,
} from "lucide-react";
import Header from "../../components/Header";
import ItemCard from "../../components/ItemCard";
import "./Messages.css";

const activeItem = {
  id: "L-4921",
  title: "Blue Hydroflask",
  type: "FOUND",
  imageUrl: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
  category: "Personal Accessories",
  locationDescription: "Near Library Main Entrance, 2nd Floor.",
  createdAt: new Date().toISOString(),
  school: { shortName: "CIT-U" }
};

const conversations = [
  {
    id: 1,
    name: "Maria Santos",
    time: "Now",
    preview: "Can we meet at the canteen?",
    img: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
    status: "online",
    active: true,
  },
  {
    id: 2,
    name: "Juan Dela Cruz",
    time: "1h",
    preview: "Is the ID still available?",
    img: "https://images.unsplash.com/photo-1599566150163-29194dcaad36?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
    status: "offline",
  },
  {
    id: 3,
    name: "Security Office",
    time: "3h",
    preview: "Item #442 verified.",
    icon: <Shield size={24} />,
    status: "system",
  },
];

const messageHistory = [
  {
    id: 1,
    type: "received",
    content: "Hi! I saw you found my Blue Tumbler. Is it still with you?",
    time: "10:30 AM",
    userImg: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
  {
    id: 2,
    type: "sent",
    content: "Yes, I have it right here. I'm on campus today.",
    time: "10:32 AM",
  },
  {
    id: 3,
    type: "location",
    content: {
      title: "Location Shared",
      point: "Meeting Point: CIT-U Canteen",
      mapImg: "https://images.unsplash.com/photo-1524661135-423995f22d0b?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
    },
    time: "10:35 AM",
    userImg: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
];

function Messages() {
  const [activeConv, setActiveConv] = useState(1);

  return (
    <div className="messages-page">
      <Header />

      <div className="messages-layout">
        {/* Left Panel */}
        <aside className="messages-sidebar">
          <div className="sidebar-header">
            <h3>Messages</h3>
            <div className="filter-tabs">
              <button className="filter-tab active">All</button>
              <button className="filter-tab">Unread</button>
            </div>
          </div>
          <div className="conversations-list">
            {conversations.map((conv) => (
              <div
                key={conv.id}
                className={`conversation-item ${activeConv === conv.id ? "active" : ""}`}
                onClick={() => setActiveConv(conv.id)}
              >
                <div className="avatar-container">
                  {conv.img ? (
                    <div
                      className="avatar-img"
                      style={{ backgroundImage: `url(${conv.img})` }}
                    ></div>
                  ) : (
                    <div className="avatar-img flex-center" style={{ backgroundColor: 'rgba(var(--color-primary-rgb), 0.1)', color: 'var(--color-primary)' }}>
                      {conv.icon}
                    </div>
                  )}
                  {conv.status === "online" && <span className="online-status"></span>}
                </div>
                <div className="conv-info">
                  <div className="conv-header">
                    <span className="conv-name">{conv.name}</span>
                    <span className="conv-time">{conv.time}</span>
                  </div>
                  <p className="conv-preview">{conv.preview}</p>
                </div>
              </div>
            ))}
          </div>
        </aside>

        {/* Center Panel */}
        <main className="chat-main">
          <div className="chat-header">
            <div className="chat-user-info">
              <h3 className="chat-user-name">Maria Santos</h3>
              <span className="status-indicator">Online</span>
            </div>
            <button className="input-icon-btn">
              <MoreVertical size={20} />
            </button>
          </div>

          <div className="chat-messages">
            <div className="date-separator">
              <span className="date-text">Today</span>
            </div>

            {messageHistory.map((msg) => (
              <div key={msg.id} className={`message ${msg.type === "sent" ? "sent" : "received"}`}>
                {msg.type !== "sent" && msg.userImg && (
                  <div
                    className="msg-avatar"
                    style={{ backgroundImage: `url(${msg.userImg})` }}
                  ></div>
                )}
                
                <div className="msg-content-wrapper">
                  {msg.type === "location" ? (
                    <div className="location-card">
                      <div className="map-wrapper">
                        <img src={msg.content.mapImg} alt="Map" className="map-img" />
                        <div className="map-pin">
                          <MapPin size={20} />
                        </div>
                      </div>
                      <div className="location-details">
                        <p className="location-name">{msg.content.title}</p>
                        <p className="location-description">{msg.content.point}</p>
                      </div>
                    </div>
                  ) : (
                    <div className="bubble">{msg.content}</div>
                  )}
                  <span className="msg-time">{msg.time}</span>
                </div>
              </div>
            ))}
          </div>

          {/* Quick Replies */}
          <div className="quick-replies">
            <button className="reply-btn">
              <CheckCircle size={14} /> I'm here
            </button>
            <button className="reply-btn">
              <Clock size={14} /> Running late
            </button>
            <button className="reply-btn">
              <Shield size={14} /> Meeting at Security Office
            </button>
          </div>

          {/* Input Area */}
          <div className="chat-input-area">
            <div className="input-container">
              <button className="input-icon-btn">
                <PlusCircle size={20} />
              </button>
              <button className="input-icon-btn">
                <ImageIcon size={20} />
              </button>
              <input
                type="text"
                placeholder="Type a message..."
                className="message-input"
              />
              <button className="send-btn">
                <Send size={18} />
              </button>
            </div>
          </div>
        </main>

        {/* Right Panel */}
        <aside className="messages-details">
          <div className="details-header">
            <h3>Item Snapshot</h3>
            <span className="ref-id">Reference ID: #L-4921</span>
          </div>
          
          <div className="details-content">
            <ItemCard item={activeItem} variant="snapshot" />

            <div className="info-section">
              <span className="info-label">Secret Detail Status</span>
              <div className="verification-status">
                <div className="status-icon"><Check size={14} /></div>
                <div>
                  <p className="status-title">Verification Successful</p>
                  <p className="status-desc">Claimant correctly identified the "Sticker on the bottom".</p>
                </div>
              </div>
            </div>

            <div className="details-actions">
              <button className="btn btn-primary btn-full btn-black">
                <CheckCircle size={18} style={{ marginRight: '0.5rem' }} /> Mark as Returned
              </button>
              <button className="btn btn-outline btn-full btn-danger-outline">
                Report User
              </button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}

export default Messages;
