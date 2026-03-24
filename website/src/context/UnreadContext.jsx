import { createContext, useContext, useEffect, useState, useCallback, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import authService from '../services/authService';
import chatService from '../services/chatService';
import notificationService from '../services/notificationService';

const WS_URL = import.meta.env.VITE_API_URL?.replace('/api', '') || 'http://localhost:8080';

const UnreadContext = createContext();

export const UnreadProvider = ({ children }) => {
    const [unreadNotifications, setUnreadNotifications] = useState(0);
    const [unreadMessages, setUnreadMessages] = useState(0);
    const stompClientRef = useRef(null);
    const activeChatIdRef = useRef(null);
    const user = authService.getCurrentUser();
    const isAuthenticated = authService.isAuthenticated();
    const isPublicAuthPage = typeof window !== 'undefined'
        && ['/login', '/register', '/forgot-password', '/verify-otp', '/reset-password']
            .includes(window.location.pathname);

    const isNotificationsEnabled = () => {
        const stored = localStorage.getItem('notificationsEnabled');
        return stored === null ? true : stored === 'true';
    };

    const fetchUnread = useCallback(() => {
        if (isPublicAuthPage || !isAuthenticated || !user) return;
        chatService.getUnreadCount().then(result => {
            if (result.success) setUnreadMessages(result.data);
        });
        if (isNotificationsEnabled()) {
            notificationService.getUnreadCount().then(result => {
                if (result.success) setUnreadNotifications(result.data);
            });
        } else {
            setUnreadNotifications(0);
        }
    }, [isPublicAuthPage, isAuthenticated, user]);

    // Fetch once on mount + poll every 60s (as safety net alongside WebSocket)
    useEffect(() => {
        if (isPublicAuthPage || !isAuthenticated || !user) return;

        const initialFetch = setTimeout(() => {
            fetchUnread();
        }, 0);

        const interval = setInterval(() => {
            if (document.visibilityState === 'visible') {
                fetchUnread();
            }
        }, 60000);

        const handleVisibilityChange = () => {
            if (document.visibilityState === 'visible') fetchUnread();
        };
        document.addEventListener('visibilitychange', handleVisibilityChange);

        return () => {
            clearTimeout(initialFetch);
            clearInterval(interval);
            document.removeEventListener('visibilitychange', handleVisibilityChange);
        };
    }, [fetchUnread, isPublicAuthPage, isAuthenticated, user]);

    // WebSocket for real-time notification pushes
    useEffect(() => {
        if (isPublicAuthPage || !isAuthenticated || !user) return;

        const token = localStorage.getItem('token');
        if (!token) return;

        const client = new Client({
            webSocketFactory: () => new SockJS(`${WS_URL}/ws`),
            connectHeaders: { Authorization: `Bearer ${token}` },
            reconnectDelay: 5000,
            onConnect: () => {
                client.subscribe('/user/queue/notifications', (frame) => {
                    if (!isNotificationsEnabled()) return;
                    try {
                        const payload = JSON.parse(frame.body);
                        // Suppress badge increment if this is for the active chat
                        if (payload.type === 'NEW_MESSAGE' && payload.linkId === activeChatIdRef.current) {
                            return;
                        }
                    } catch {
                        // If parsing fails, still increment
                    }
                    setUnreadNotifications(prev => prev + 1);
                });
                client.subscribe('/user/queue/messages', (frame) => {
                    try {
                        const payload = JSON.parse(frame.body);
                        // Suppress badge increment if this notification is for the active chat
                        if (payload.linkId === activeChatIdRef.current) {
                            return;
                        }
                    } catch {
                        // If parsing fails, still increment
                    }
                    setUnreadMessages(prev => prev + 1);
                });
            },
            onStompError: () => {},
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            if (stompClientRef.current?.active) {
                stompClientRef.current.deactivate();
            }
        };
    }, [isPublicAuthPage, isAuthenticated, user]);

    const refreshNotificationCount = useCallback(() => {
        if (isPublicAuthPage || !isAuthenticated || !user) return;
        notificationService.getUnreadCount().then(result => {
            if (result.success) setUnreadNotifications(result.data);
        });
    }, [isPublicAuthPage, isAuthenticated, user]);

    const refreshMessageCount = useCallback(() => {
        if (isPublicAuthPage || !isAuthenticated || !user) return;
        chatService.getUnreadCount().then(result => {
            if (result.success) setUnreadMessages(result.data);
        });
    }, [isPublicAuthPage, isAuthenticated, user]);

    const handleLogout = useCallback(() => {
        if (stompClientRef.current?.active) {
            stompClientRef.current.deactivate();
        }
    }, []);

    // Allow Messages page to suppress unread badge increment for the active chat
    const setActiveChatForBadge = useCallback((chatId) => {
        activeChatIdRef.current = chatId;
    }, []);

    return (
        <UnreadContext.Provider value={{
            unreadNotifications,
            unreadMessages,
            refreshNotificationCount,
            refreshMessageCount,
            fetchUnread,
            handleLogout,
            setActiveChatForBadge,
        }}>
            {children}
        </UnreadContext.Provider>
    );
};

export const useUnread = () => {
    const context = useContext(UnreadContext);
    if (!context) {
        throw new Error('useUnread must be used within an UnreadProvider');
    }
    return context;
};
