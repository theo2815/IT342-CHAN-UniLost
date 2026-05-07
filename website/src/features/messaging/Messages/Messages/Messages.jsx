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
  HandMetal,
  PackageCheck,
  Info,
  Star,
  FileText,
  CheckCircle,
  XCircle,
  Clock,
  ArrowRight,
  Trophy,
  ShieldAlert,
} from "lucide-react";
import { useSearchParams } from "react-router-dom";
import SockJS from "sockjs-client/dist/sockjs";
import { Client } from "@stomp/stompjs";
import Header from "../../components/Header";
import StatusBadge from "../../components/StatusBadge";
import chatService from "../../services/chatService";
import claimService from "../../services/claimService";
import authService from "../../services/authService";
import ConfirmDialog from "../../components/ConfirmDialog";
import { useToast } from "../../hooks/useToast";
import { useUnread } from "../../context/UnreadContext";
import "./Messages.css";

function Messages() {
  const [searchParams] = useSearchParams();
  const initialChatId = searchParams.get("chatId");
  const toast = useToast();
  const { setActiveChatForBadge, refreshMessageCount } = useUnread();

  const [chats, setChats] = useState([]);
  const [activeChatId, setActiveChatId] = useState(initialChatId || null);
  const [activeChat, setActiveChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [loadingOlder, setLoadingOlder] = useState(false);
  const [messagesPage, setMessagesPage] = useState(0);
  const [hasMoreMessages, setHasMoreMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [handoverLoading, setHandoverLoading] = useState(false);
  const [claimActionLoading, setClaimActionLoading] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState({ isOpen: false });
  const [filter, setFilter] = useState("all");
  const [showMobileSidebar, setShowMobileSidebar] = useState(!initialChatId);
  const [wsStatus, setWsStatus] = useState("connected"); // "connected" | "reconnecting" | "disconnected"

  const messagesEndRef = useRef(null);
  const chatContainerRef = useRef(null);
  const stompClientRef = useRef(null);
  const activeChatIdRef = useRef(activeChatId);
  const markReadTimerRef = useRef(null);
  const currentUser = authService.getCurrentUser();

  // Keep ref in sync
  useEffect(() => {
    activeChatIdRef.current = activeChatId;
    // Suppress unread badge increment for the active chat
    setActiveChatForBadge(activeChatId);
    return () => setActiveChatForBadge(null);
  }, [activeChatId, setActiveChatForBadge]);

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
    setMessagesPage(0);
    setHasMoreMessages(false);
    const result = await chatService.getMessages(chatId, 0, 50);
    if (result.success && activeChatIdRef.current === chatId) {
      const reversed = [...(result.data.content || [])].reverse();
      setMessages(reversed);
      setHasMoreMessages(!result.data.last);
      setMessagesPage(0);
    }
    if (activeChatIdRef.current === chatId) {
      setMessagesLoading(false);
    }
  };

  const loadOlderMessages = async () => {
    if (loadingOlder || !hasMoreMessages || !activeChatId) return;
    const chatId = activeChatId;
    const nextPage = messagesPage + 1;
    setLoadingOlder(true);
    const result = await chatService.getMessages(chatId, nextPage, 50);
    if (result.success && activeChatIdRef.current === chatId) {
      const container = chatContainerRef.current;
      const prevScrollHeight = container?.scrollHeight || 0;
      const olderMessages = [...(result.data.content || [])].reverse();
      setMessages(prev => [...olderMessages, ...prev]);
      setMessagesPage(nextPage);
      setHasMoreMessages(!result.data.last);
      // Preserve scroll position after prepending older messages
      requestAnimationFrame(() => {
        if (container) {
          container.scrollTop = container.scrollHeight - prevScrollHeight;
        }
      });
    }
    setLoadingOlder(false);
  };

  // C2 fix: debounce markRead to avoid flooding server
  const debouncedMarkRead = (chatId) => {
    if (markReadTimerRef.current) clearTimeout(markReadTimerRef.current);
    markReadTimerRef.current = setTimeout(async () => {
      await chatService.markAsRead(chatId);
      setChats(prev => prev.map(c =>
        c.id === chatId ? { ...c, unreadCount: 0 } : c
      ));
      refreshMessageCount();
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

  // Load older messages on scroll-to-top
  const handleMessagesScroll = () => {
    const container = chatContainerRef.current;
    if (!container || loadingOlder || !hasMoreMessages) return;
    if (container.scrollTop < 60) {
      loadOlderMessages();
    }
  };

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
        setWsStatus("connected");
        client.subscribe(`/topic/chat/${activeChatId}`, (frame) => {
          const msg = JSON.parse(frame.body);
          const msgType = msg.type || "TEXT";
          // Accept system messages (no senderId) and messages from other users
          if (msgType !== "TEXT" || !currentUser?.id || msg.senderId !== currentUser.id) {
            setMessages(prev => [...prev, msg]);
            if (msg.senderId && msg.senderId !== currentUser?.id) {
              debouncedMarkRead(activeChatId);
            }
            // Refresh chat detail for status changes
            if (msgType === "HANDOVER_REQUEST" || msgType === "HANDOVER_CONFIRMED"
                || msgType === "HANDOVER_DISPUTED" || msgType === "CLAIM_ACCEPTED" || msgType === "CLAIM_REJECTED") {
              loadChatDetail(activeChatId);
              loadChats();
            }
          }
        });
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers?.message);
        setWsStatus("disconnected");
      },
      onWebSocketError: (error) => {
        console.error("WebSocket error:", error);
        setWsStatus("reconnecting");
      },
      onDisconnect: () => {
        setWsStatus("reconnecting");
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
      toast.error(result.error || "Message failed to send. Please try again.");
    }
    setSending(false);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
    // Shift+Enter allows newline in textarea (default behavior)
  };

  const handleMarkReturned = () => {
    if (!activeChat?.claimId || handoverLoading) return;
    setConfirmDialog({
      isOpen: true,
      title: "Mark Item as Returned?",
      message: "Confirm that you have physically handed the item to the owner. They will be asked to verify receipt.",
      confirmLabel: "Yes, I Returned It",
      variant: "warning",
      onConfirm: async () => {
        setHandoverLoading(true);
        const result = await claimService.markItemReturned(activeChat.claimId);
        if (result.success) {
          await loadChatDetail(activeChatId);
          loadChats();
        }
        setHandoverLoading(false);
      },
    });
  };

  const handleConfirmReceived = () => {
    if (!activeChat?.claimId || handoverLoading) return;
    setConfirmDialog({
      isOpen: true,
      title: "Confirm Item Received?",
      message: "Confirm that you have received your item. This will complete the handover and award karma to both parties.",
      confirmLabel: "Yes, I Received It",
      variant: "success",
      onConfirm: async () => {
        setHandoverLoading(true);
        const result = await claimService.confirmItemReceived(activeChat.claimId);
        if (result.success) {
          await loadChatDetail(activeChatId);
          loadChats();
        }
        setHandoverLoading(false);
      },
    });
  };

  const handleDisputeHandover = () => {
    if (!activeChat?.claimId || handoverLoading) return;
    setConfirmDialog({
      isOpen: true,
      title: "Report Problem?",
      message: "Report that you did not receive the item. The handover status will be reverted so the other party can try again.",
      confirmLabel: "I Did Not Receive It",
      variant: "danger",
      onConfirm: async () => {
        setHandoverLoading(true);
        const result = await claimService.disputeHandover(activeChat.claimId);
        if (result.success) {
          await loadChatDetail(activeChatId);
          loadChats();
        } else {
          toast.error(result.error || "Failed to dispute handover. Please try again.");
        }
        setHandoverLoading(false);
      },
    });
  };

  const handleAcceptClaim = async () => {
    if (!activeChat?.claimId || claimActionLoading) return;
    setClaimActionLoading(true);
    const result = await claimService.acceptClaim(activeChat.claimId);
    if (result.success) {
      await loadChatDetail(activeChatId);
      loadChats();
    } else {
      toast.error(result.error || "Failed to accept claim. Please try again.");
    }
    setClaimActionLoading(false);
  };

  const handleRejectClaim = async () => {
    if (!activeChat?.claimId || claimActionLoading) return;
    setClaimActionLoading(true);
    const result = await claimService.rejectClaim(activeChat.claimId);
    if (result.success) {
      await loadChatDetail(activeChatId);
      loadChats();
    } else {
      toast.error(result.error || "Failed to reject claim. Please try again.");
    }
    setClaimActionLoading(false);
  };

  // Determine which handover action to show
  // For LOST items, roles are inverted: finderId=poster=actual owner, ownerId=claimant=actual finder
  const isFinder = activeChat?.finderId === currentUser?.id;
  const isOwner = activeChat?.ownerId === currentUser?.id;
  const isLostItem = activeChat?.itemType === "LOST";
  const isActualHolder = isLostItem ? isOwner : isFinder;   // person who has the physical item
  const isActualOwner = isLostItem ? isFinder : isOwner;     // person who owns the item
  const showMarkReturned = isActualHolder
    && activeChat?.claimStatus === "ACCEPTED"
    && activeChat?.itemStatus === "CLAIMED";
  const showConfirmReceived = isActualOwner
    && activeChat?.claimStatus === "ACCEPTED"
    && activeChat?.itemStatus === "PENDING_OWNER_CONFIRMATION";

  const isHandoverComplete = activeChat?.claimStatus === "COMPLETED";
  const isClaimRejected = activeChat?.claimStatus === "REJECTED";
  const isClaimCancelled = activeChat?.claimStatus === "CANCELLED";
  const isChatEnded = isHandoverComplete || isClaimRejected || isClaimCancelled;

  const getProgressSteps = () => {
    if (!activeChat) return [];
    const cs = activeChat.claimStatus;
    const is = activeChat.itemStatus;
    return [
      { label: "Claim Filed", done: true },
      { label: cs === "REJECTED" ? "Rejected" : "Reviewed", done: ["ACCEPTED", "COMPLETED", "REJECTED"].includes(cs), rejected: cs === "REJECTED" },
      { label: "Returned", done: is === "PENDING_OWNER_CONFIRMATION" || is === "RETURNED", active: cs === "ACCEPTED" && is === "CLAIMED" },
      { label: "Confirmed", done: cs === "COMPLETED", active: is === "PENDING_OWNER_CONFIRMATION" },
    ];
  };

  const getNextActionInfo = () => {
    if (!activeChat) return null;
    const cs = activeChat.claimStatus;
    const is = activeChat.itemStatus;
    if (cs === "COMPLETED") return { text: "Handover complete! Both parties earned karma.", type: "success" };
    if (cs === "REJECTED") return { text: "This claim was rejected.", type: "error" };
    if (cs === "CANCELLED") return { text: "This claim was cancelled.", type: "muted" };
    if (is === "PENDING_OWNER_CONFIRMATION") {
      return isActualOwner
        ? { text: "Your turn — confirm you received the item, or report a problem.", type: "action" }
        : { text: "Waiting for the owner to confirm receipt.", type: "waiting" };
    }
    if (cs === "ACCEPTED" && is === "CLAIMED") {
      return isActualHolder
        ? { text: "Your turn — meet up and mark the item as returned.", type: "action" }
        : { text: "Waiting for the item to be returned to you.", type: "waiting" };
    }
    if (cs === "PENDING" && !isLostItem) {
      return isFinder
        ? { text: "Your turn — review this claim and accept or reject.", type: "action" }
        : { text: "Waiting for the finder to review your claim.", type: "waiting" };
    }
    if (cs === "PENDING" && isLostItem) {
      return { text: "Claim submitted — waiting for auto-verification.", type: "waiting" };
    }
    return null;
  };

  const getChatStatusType = (chat) => {
    if (chat.claimStatus === "COMPLETED") return "completed";
    if (chat.claimStatus === "REJECTED" || chat.claimStatus === "CANCELLED") return "closed";
    if (chat.itemStatus === "PENDING_OWNER_CONFIRMATION") return "pending";
    return "active";
  };

  const progressSteps = getProgressSteps();
  const nextAction = getNextActionInfo();

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

  const formatDateSeparator = (dateStr) => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    if (date.toDateString() === today.toDateString()) return "Today";
    if (date.toDateString() === yesterday.toDateString()) return "Yesterday";
    return date.toLocaleDateString(undefined, { year: "numeric", month: "long", day: "numeric" });
  };

  const filteredChats = (filter === "unread"
    ? chats.filter(c => c.unreadCount > 0)
    : [...chats]
  ).sort((a, b) => new Date(b.lastMessageAt || 0) - new Date(a.lastMessageAt || 0));

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
              filteredChats.map((chat) => {
                const statusType = getChatStatusType(chat);
                return (
                  <div
                    key={chat.id}
                    className={`conversation-item ${activeChatId === chat.id ? "active" : ""} conv-${statusType}`}
                    onClick={() => selectChat(chat.id)}
                  >
                    <div className="avatar-container">
                      <div className="avatar-img avatar-initials">
                        {(chat.otherParticipantName || "?").charAt(0).toUpperCase()}
                      </div>
                      <span className={`conv-status-dot status-${statusType}`} />
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
                );
              })
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
                      {activeChat.itemStatus && (
                        <StatusBadge status={activeChat.itemStatus} />
                      )}
                    </span>
                  )}
                </div>
              </div>

              {/* Progress Tracker */}
              {activeChat && !isClaimCancelled && (
                <div className="handover-progress">
                  <div className="progress-tracker">
                    {progressSteps.map((step, i) => (
                      <div key={i} className={`tracker-step ${step.done ? "done" : ""} ${step.active ? "current" : ""} ${step.rejected ? "rejected" : ""}`}>
                        <div className="tracker-step-top">
                          <div className={`tracker-line${i > 0 && progressSteps[i - 1].done ? " filled" : ""}${i === 0 ? " invisible" : ""}`} />
                          <div className="tracker-dot">
                            {step.done && !step.rejected ? <Check size={10} /> : step.rejected ? <XCircle size={10} /> : null}
                          </div>
                          <div className={`tracker-line${i < progressSteps.length - 1 && step.done ? " filled" : ""}${i === progressSteps.length - 1 ? " invisible" : ""}`} />
                        </div>
                        <span className="tracker-label">{step.label}</span>
                      </div>
                    ))}
                  </div>
                  {nextAction && (
                    <div className={`next-action-banner ${nextAction.type}`}>
                      {nextAction.type === "action" && <ArrowRight size={13} />}
                      {nextAction.type === "waiting" && <Clock size={13} />}
                      {nextAction.type === "success" && <Trophy size={13} />}
                      {nextAction.type === "error" && <XCircle size={13} />}
                      <span>{nextAction.text}</span>
                    </div>
                  )}
                </div>
              )}

              <div className="chat-messages" ref={chatContainerRef} onScroll={handleMessagesScroll}>
                {loadingOlder && (
                  <div className="loading-older">
                    <Loader size={16} className="spin" />
                    <span>Loading older messages...</span>
                  </div>
                )}
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
                  messages.map((msg, idx) => {
                    const msgType = msg.type || "TEXT";
                    const isSent = msg.senderId === currentUser?.id;

                    // Date separator between messages on different days
                    let dateSep = null;
                    if (msg.createdAt) {
                      const msgDate = new Date(msg.createdAt).toDateString();
                      const prevDate = idx > 0 && messages[idx - 1].createdAt
                        ? new Date(messages[idx - 1].createdAt).toDateString()
                        : null;
                      if (msgDate !== prevDate) {
                        dateSep = (
                          <div key={`sep-${msg.id}`} className="date-separator">
                            <span>{formatDateSeparator(msg.createdAt)}</span>
                          </div>
                        );
                      }
                    }

                    // System / structured messages
                    if (msgType === "SYSTEM" || msgType === "HANDOVER_REQUEST" || msgType === "HANDOVER_CONFIRMED"
                        || msgType === "HANDOVER_DISPUTED" || msgType === "CLAIM_ACCEPTED" || msgType === "CLAIM_REJECTED") {
                      const systemMsgClass = {
                        CLAIM_ACCEPTED: "claim-accepted",
                        CLAIM_REJECTED: "claim-rejected",
                        HANDOVER_CONFIRMED: "handover-confirmed",
                        HANDOVER_DISPUTED: "handover-disputed",
                        HANDOVER_REQUEST: "handover-request",
                      }[msgType] || "";
                      return (
                        <>{dateSep}
                        <div key={msg.id} className={`system-message ${systemMsgClass}`}>
                          <div className="system-message-content">
                            {msgType === "HANDOVER_REQUEST" && <HandMetal size={16} />}
                            {msgType === "HANDOVER_CONFIRMED" && <PackageCheck size={16} />}
                            {msgType === "HANDOVER_DISPUTED" && <ShieldAlert size={16} />}
                            {msgType === "CLAIM_ACCEPTED" && <CheckCircle size={16} />}
                            {msgType === "CLAIM_REJECTED" && <XCircle size={16} />}
                            {msgType === "SYSTEM" && <Info size={16} />}
                            <span>{msg.content}</span>
                          </div>
                          {msgType === "HANDOVER_CONFIRMED" && msg.metadata?.finderKarma && (
                            <div className="karma-info">
                              <Star size={14} />
                              <span>Finder earned +{msg.metadata.finderKarma} karma, Owner earned +{msg.metadata.ownerKarma} karma</span>
                            </div>
                          )}
                          <span className="system-msg-time">{formatMessageTime(msg.createdAt)}</span>
                        </div>
                        </>
                      );
                    }

                    // Claim submission card
                    if (msgType === "CLAIM_SUBMISSION") {
                      const meta = msg.metadata || {};
                      const claimImage = meta.itemImageUrl || activeChat?.itemImageUrl;
                      return (
                        <>{dateSep}
                        <div key={msg.id} className="system-message">
                          <div className="claim-card">
                            {claimImage && (
                              <div className="claim-card-thumb">
                                <img src={claimImage} alt={meta.itemTitle || "Item"} />
                              </div>
                            )}
                            <div className="claim-card-body">
                              <div className="claim-card-title">
                                <FileText size={14} />
                                <strong>Claim Submitted</strong>
                              </div>
                              {meta.itemTitle && (
                                <div className="claim-card-field">
                                  <span className="claim-field-label">Item:</span> {meta.itemTitle}
                                </div>
                              )}
                              {meta.claimantName && (
                                <div className="claim-card-field">
                                  <span className="claim-field-label">By:</span> {meta.claimantName}
                                </div>
                              )}
                              {meta.providedAnswer && (
                                <div className="claim-card-field">
                                  <span className="claim-field-label">Verification Answer:</span> {meta.providedAnswer}
                                </div>
                              )}
                              {meta.claimMessage && (
                                <div className="claim-card-field">
                                  <span className="claim-field-label">Message:</span> {meta.claimMessage}
                                </div>
                              )}
                              {isFinder && activeChat?.claimStatus === "PENDING" && !isLostItem
                                && meta.claimId === activeChat?.claimId && (
                                <div className="claim-action-buttons">
                                  <button
                                    className="claim-action-btn accept"
                                    onClick={handleAcceptClaim}
                                    disabled={claimActionLoading}
                                  >
                                    {claimActionLoading ? <Loader size={14} className="spin" /> : <CheckCircle size={14} />}
                                    Accept Claim
                                  </button>
                                  <button
                                    className="claim-action-btn reject"
                                    onClick={handleRejectClaim}
                                    disabled={claimActionLoading}
                                  >
                                    {claimActionLoading ? <Loader size={14} className="spin" /> : <XCircle size={14} />}
                                    Reject Claim
                                  </button>
                                </div>
                              )}
                            </div>
                          </div>
                          <span className="system-msg-time">{formatMessageTime(msg.createdAt)}</span>
                        </div>
                        </>
                      );
                    }

                    // Regular TEXT message
                    return (
                      <>{dateSep}
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
                      </>
                    );
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* WebSocket connection status */}
              {wsStatus === "reconnecting" && (
                <div className="ws-status-banner reconnecting">
                  <Loader size={12} className="spin" /> Reconnecting...
                </div>
              )}
              {wsStatus === "disconnected" && (
                <div className="ws-status-banner disconnected">
                  <AlertCircle size={12} /> Connection lost. Messages may be delayed.
                </div>
              )}

              {/* Quick Replies */}
              {!isChatEnded && (
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
              )}

              {/* Handover Actions */}
              {(showMarkReturned || showConfirmReceived) && (
                <div className="handover-actions">
                  {showMarkReturned && (
                    <div className="handover-action-group">
                      <button
                        className="handover-btn mark-returned"
                        onClick={handleMarkReturned}
                        disabled={handoverLoading}
                      >
                        {handoverLoading ? <Loader size={16} className="spin" /> : <HandMetal size={16} />}
                        Mark as Returned to Owner
                      </button>
                      <span className="handover-hint">Click after you've physically handed the item over</span>
                    </div>
                  )}
                  {showConfirmReceived && (
                    <div className="handover-action-group">
                      <div className="handover-btn-row">
                        <button
                          className="handover-btn confirm-received"
                          onClick={handleConfirmReceived}
                          disabled={handoverLoading}
                        >
                          {handoverLoading ? <Loader size={16} className="spin" /> : <PackageCheck size={16} />}
                          Confirm Received
                        </button>
                        <button
                          className="handover-btn dispute-handover"
                          onClick={handleDisputeHandover}
                          disabled={handoverLoading}
                        >
                          {handoverLoading ? <Loader size={16} className="spin" /> : <ShieldAlert size={16} />}
                          Did Not Receive
                        </button>
                      </div>
                      <span className="handover-hint">The other party marked this item as returned — did you receive it?</span>
                    </div>
                  )}
                </div>
              )}

              {/* Input */}
              {isChatEnded ? (
                <div className="chat-ended-banner">
                  <Info size={16} />
                  <span>This conversation has ended.</span>
                </div>
              ) : (
                <div className="chat-input-area">
                  <div className="input-container">
                    <textarea
                      placeholder="Type a message..."
                      className="message-textarea"
                      value={newMessage}
                      onChange={(e) => {
                        setNewMessage(e.target.value);
                        // Auto-expand textarea
                        e.target.style.height = "auto";
                        e.target.style.height = Math.min(e.target.scrollHeight, 120) + "px";
                      }}
                      onKeyDown={handleKeyDown}
                      maxLength={2000}
                      rows={1}
                    />
                    <button
                      className="send-btn"
                      onClick={handleSend}
                      disabled={!newMessage.trim() || sending}
                    >
                      <Send size={18} />
                    </button>
                  </div>
                  {newMessage.length > 1800 && (
                    <div className={`char-counter ${newMessage.length >= 2000 ? "at-limit" : "near-limit"}`}>
                      {2000 - newMessage.length} characters remaining
                    </div>
                  )}
                </div>
              )}
            </>
          )}
        </main>
      </div>

      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        onClose={() => setConfirmDialog({ isOpen: false })}
        onConfirm={confirmDialog.onConfirm || (() => {})}
        title={confirmDialog.title}
        message={confirmDialog.message}
        confirmLabel={confirmDialog.confirmLabel}
        variant={confirmDialog.variant}
      />
    </div>
  );
}

export default Messages;
