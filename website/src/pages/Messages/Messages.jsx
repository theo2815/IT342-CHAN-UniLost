import { useState, useEffect, useRef } from "react";
import {
  Send,
  Check,
  CheckCheck,
  Loader,
  MessageSquare,
  ChevronLeft,
  Package,
  AlertCircle,
} from "lucide-react";
import { useSearchParams } from "react-router-dom";
import SockJS from "sockjs-client/dist/sockjs";
import { Client } from "@stomp/stompjs";
import Header from "../../components/Header";
import chatService from "../../services/chatService";
import authService from "../../services/authService";
import "./Messages.css";

function Messages() {
  const [searchParams] = useSearchParams();
  const initialChatId = searchParams.get("chatId");

  const [chats, setChats] = useState([]);
  const [activeChatId, setActiveChatId] = useState(initialChatId || null);
  const [activeChat, setActiveChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [filter, setFilter] = useState("all");
  const [showMobileSidebar, setShowMobileSidebar] = useState(!initialChatId);

  const messagesEndRef = useRef(null);
  const chatContainerRef = useRef(null);
  const stompClientRef = useRef(null);
  const activeChatIdRef = useRef(activeChatId);
  const markReadTimerRef = useRef(null);
  const currentUser = authService.getCurrentUser();

  // Keep ref in sync
  useEffect(() => {
    activeChatIdRef.current = activeChatId;
  }, [activeChatId]);

  // Load chat list
  useEffect(() => {
    loadChats();
  }, []);

  const loadChats = async () => {
    setLoading(true);
    setLoadError("");
    const result = await chatService.getMyChats();
    if (result.success) {
      setChats(result.data);
      if (initialChatId && !activeChatIdRef.current) {
        setActiveChatId(initialChatId);
      }
    } else {
      setLoadError(result.error);
    }
    setLoading(false);
  };

  // Load messages when active chat changes
  useEffect(() => {
    if (!activeChatId) {
      setMessages([]);
      setActiveChat(null);
      return;
    }
    loadMessages(activeChatId);
    loadChatDetail(activeChatId);
    debouncedMarkRead(activeChatId);
  }, [activeChatId]);

  const loadChatDetail = async (chatId) => {
    const result = await chatService.getChatById(chatId);
    if (result.success && activeChatIdRef.current === chatId) {
      setActiveChat(result.data);
    }
  };

  // H1 fix: check chatId is still active before setting state
  const loadMessages = async (chatId) => {
    setMessagesLoading(true);
    const result = await chatService.getMessages(chatId);
    if (result.success && activeChatIdRef.current === chatId) {
      const reversed = [...(result.data.content || [])].reverse();
      setMessages(reversed);
    }
    if (activeChatIdRef.current === chatId) {
      setMessagesLoading(false);
    }
  };

  // C2 fix: debounce markRead to avoid flooding server
  const debouncedMarkRead = (chatId) => {
    if (markReadTimerRef.current) clearTimeout(markReadTimerRef.current);
    markReadTimerRef.current = setTimeout(async () => {
      await chatService.markAsRead(chatId);
      setChats(prev => prev.map(c =>
        c.id === chatId ? { ...c, unreadCount: 0 } : c
      ));
    }, 500);
  };

  // M2 fix: only auto-scroll if user is near bottom
  useEffect(() => {
    const container = chatContainerRef.current;
    if (!container) return;
    const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 150;
    if (isNearBottom) {
      messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  // WebSocket connection — C1 fix: send auth token
  useEffect(() => {
    if (!activeChatId) return;

    const token = localStorage.getItem("token");
    const apiBase = import.meta.env.VITE_API_URL || "http://localhost:8080/api";
    const wsUrl = apiBase.replace("/api", "") + "/ws";

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/chat/${activeChatId}`, (frame) => {
          const msg = JSON.parse(frame.body);
          // M1 fix: guard against null currentUser
          if (currentUser?.id && msg.senderId !== currentUser.id) {
            setMessages(prev => [...prev, msg]);
            debouncedMarkRead(activeChatId);
          }
        });
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers?.message);
      },
      onWebSocketError: (error) => {
        console.error("WebSocket error:", error);
      },
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
      if (markReadTimerRef.current) {
        clearTimeout(markReadTimerRef.current);
      }
    };
  }, [activeChatId]);

  // H2 fix: rollback optimistic message on failure
  const handleSend = async () => {
    if (!newMessage.trim() || !activeChatId || sending) return;
    const content = newMessage.trim();
    setNewMessage("");
    setSending(true);

    const optimisticId = "temp-" + Date.now() + "-" + Math.random().toString(36).slice(2, 7);
    const optimisticMsg = {
      id: optimisticId,
      chatId: activeChatId,
      senderId: currentUser?.id,
      senderName: currentUser?.fullName,
      content,
      read: false,
      createdAt: new Date().toISOString(),
    };
    setMessages(prev => [...prev, optimisticMsg]);

    const result = await chatService.sendMessage(activeChatId, content);
    if (result.success) {
      setMessages(prev =>
        prev.map(m => m.id === optimisticId ? result.data : m)
      );
      setChats(prev => prev.map(c =>
        c.id === activeChatId
          ? { ...c, lastMessagePreview: content, lastMessageAt: new Date().toISOString() }
          : c
      ));
    } else {
      // Rollback: remove the optimistic message
      setMessages(prev => prev.filter(m => m.id !== optimisticId));
    }
    setSending(false);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const selectChat = (chatId) => {
    setActiveChatId(chatId);
    setShowMobileSidebar(false);
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins < 1) return "Now";
    if (diffMins < 60) return `${diffMins}m`;
    const diffHrs = Math.floor(diffMins / 60);
    if (diffHrs < 24) return `${diffHrs}h`;
    const diffDays = Math.floor(diffHrs / 24);
    if (diffDays < 7) return `${diffDays}d`;
    return date.toLocaleDateString();
  };

  const formatMessageTime = (dateStr) => {
    if (!dateStr) return "";
    return new Date(dateStr).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  const filteredChats = filter === "unread"
    ? chats.filter(c => c.unreadCount > 0)
    : chats;

  return (
    <div className="messages-page">
      <Header />

      <div className="messages-layout">
        {/* Left Panel - Chat List */}
        <aside className={`messages-sidebar ${showMobileSidebar ? "mobile-show" : ""}`}>
          <div className="sidebar-header">
            <h3>Messages</h3>
            <div className="filter-tabs">
              <button
                className={`filter-tab ${filter === "all" ? "active" : ""}`}
                onClick={() => setFilter("all")}
              >
                All
              </button>
              <button
                className={`filter-tab ${filter === "unread" ? "active" : ""}`}
                onClick={() => setFilter("unread")}
              >
                Unread
              </button>
            </div>
          </div>
          <div className="conversations-list">
            {loading ? (
              <div className="chat-empty-state">
                <Loader size={24} className="spin" />
                <span>Loading chats...</span>
              </div>
            ) : loadError ? (
              <div className="chat-empty-state">
                <AlertCircle size={32} />
                <p>Failed to load chats</p>
                <span className="text-muted">{loadError}</span>
                <button className="reply-btn" onClick={loadChats} style={{ marginTop: "0.5rem" }}>
                  Retry
                </button>
              </div>
            ) : filteredChats.length === 0 ? (
              <div className="chat-empty-state">
                <MessageSquare size={32} />
                <p>{filter === "unread" ? "No unread messages" : "No conversations yet"}</p>
                <span className="text-muted">Chats are created when you submit a claim on an item</span>
              </div>
            ) : (
              filteredChats.map((chat) => (
                <div
                  key={chat.id}
                  className={`conversation-item ${activeChatId === chat.id ? "active" : ""}`}
                  onClick={() => selectChat(chat.id)}
                >
                  <div className="avatar-container">
                    <div className="avatar-img avatar-initials">
                      {(chat.otherParticipantName || "?").charAt(0).toUpperCase()}
                    </div>
                  </div>
                  <div className="conv-info">
                    <div className="conv-header">
                      <span className="conv-name">{chat.otherParticipantName}</span>
                      <span className="conv-time">{formatTime(chat.lastMessageAt)}</span>
                    </div>
                    <p className="conv-preview">
                      {chat.itemTitle && (
                        <span className="conv-item-tag">{chat.itemTitle} — </span>
                      )}
                      {chat.lastMessagePreview || "No messages yet"}
                    </p>
                  </div>
                  {chat.unreadCount > 0 && (
                    <span className="unread-badge">{chat.unreadCount}</span>
                  )}
                </div>
              ))
            )}
          </div>
        </aside>

        {/* Center Panel - Chat Messages */}
        <main className="chat-main">
          {!activeChatId ? (
            <div className="chat-placeholder">
              <MessageSquare size={48} />
              <h3>Select a conversation</h3>
              <p>Choose a chat from the sidebar to start messaging</p>
            </div>
          ) : (
            <>
              <div className="chat-header">
                <div className="chat-user-info">
                  <button className="mobile-back-btn" onClick={() => setShowMobileSidebar(true)}>
                    <ChevronLeft size={20} />
                  </button>
                  <h3 className="chat-user-name">
                    {activeChat?.otherParticipantName || "Loading..."}
                  </h3>
                  {activeChat?.itemTitle && (
                    <span className="chat-item-label">
                      <Package size={14} />
                      {activeChat.itemTitle}
                    </span>
                  )}
                </div>
              </div>

              <div className="chat-messages" ref={chatContainerRef}>
                {messagesLoading ? (
                  <div className="chat-empty-state">
                    <Loader size={24} className="spin" />
                    <span>Loading messages...</span>
                  </div>
                ) : messages.length === 0 ? (
                  <div className="chat-empty-state">
                    <MessageSquare size={32} />
                    <p>No messages yet</p>
                    <span className="text-muted">Send a message to start the conversation</span>
                  </div>
                ) : (
                  messages.map((msg) => {
                    const isSent = msg.senderId === currentUser?.id;
                    return (
                      <div key={msg.id} className={`message ${isSent ? "sent" : "received"}`}>
                        {!isSent && (
                          <div className="msg-avatar avatar-initials small">
                            {(msg.senderName || "?").charAt(0).toUpperCase()}
                          </div>
                        )}
                        <div className="msg-content-wrapper">
                          <div className="bubble">{msg.content}</div>
                          <span className="msg-time">
                            {formatMessageTime(msg.createdAt)}
                            {isSent && (
                              <span className="read-receipt">
                                {msg.read ? <CheckCheck size={14} /> : <Check size={14} />}
                              </span>
                            )}
                          </span>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Quick Replies */}
              <div className="quick-replies">
                <button className="reply-btn" onClick={() => setNewMessage("I'm here now")}>
                  <Check size={14} /> I'm here
                </button>
                <button className="reply-btn" onClick={() => setNewMessage("Running a bit late, be there soon")}>
                  Running late
                </button>
                <button className="reply-btn" onClick={() => setNewMessage("Can we meet at the security office?")}>
                  Meet at Security
                </button>
              </div>

              {/* Input */}
              <div className="chat-input-area">
                <div className="input-container">
                  <input
                    type="text"
                    placeholder="Type a message..."
                    className="message-input"
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    onKeyDown={handleKeyDown}
                    maxLength={2000}
                  />
                  <button
                    className="send-btn"
                    onClick={handleSend}
                    disabled={!newMessage.trim() || sending}
                  >
                    <Send size={18} />
                  </button>
                </div>
              </div>
            </>
          )}
        </main>
      </div>
    </div>
  );
}

export default Messages;
