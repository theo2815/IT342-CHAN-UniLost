// Mock Notifications Data

export const mockNotifications = [
    {
        id: 'n1',
        type: 'CLAIM_RECEIVED',
        title: 'New claim on your item',
        message: "Maria Santos claimed your 'Black Samsung Galaxy S24'. Review their claim to verify ownership.",
        isRead: false,
        createdAt: '2025-02-24T08:30:00',
        linkTo: '/my-items/1/claims',
    },
    {
        id: 'n2',
        type: 'CLAIM_APPROVED',
        title: 'Your claim was approved!',
        message: "Great news! Your claim on 'Student ID Card - USJ-R' was approved. Please arrange a handover.",
        isRead: false,
        createdAt: '2025-02-23T15:30:00',
        linkTo: '/claims/c3',
    },
    {
        id: 'n3',
        type: 'HANDOVER_REMINDER',
        title: 'Complete your handover',
        message: "Reminder: You have a pending handover for 'Student ID Card - USJ-R'. Meet at the campus security office.",
        isRead: false,
        createdAt: '2025-02-23T10:00:00',
        linkTo: '/claims/c3',
    },
    {
        id: 'n4',
        type: 'CLAIM_RECEIVED',
        title: 'New claim on your item',
        message: "Carlos Reyes claimed your 'Black Samsung Galaxy S24'. Check their secret detail answer.",
        isRead: false,
        createdAt: '2025-02-22T16:45:00',
        linkTo: '/my-items/1/claims',
    },
    {
        id: 'n5',
        type: 'ITEM_MATCH',
        title: 'Possible match found',
        message: "A new found item 'Blue Hydroflask' was posted at UC Main that may match your lost report.",
        isRead: true,
        createdAt: '2025-02-22T09:15:00',
        linkTo: '/items/5',
    },
    {
        id: 'n6',
        type: 'CLAIM_REJECTED',
        title: 'Claim not approved',
        message: "Your claim on 'Red Nike Backpack' was not approved by the poster. You can browse other items.",
        isRead: true,
        createdAt: '2025-02-21T14:00:00',
        linkTo: '/claims/c5',
    },
    {
        id: 'n7',
        type: 'HANDOVER_CONFIRMED',
        title: 'Handover complete!',
        message: "The handover for 'Calculator (Casio fx-991)' has been confirmed by both parties. Glad we could help!",
        isRead: true,
        createdAt: '2025-02-20T11:30:00',
        linkTo: '/claims/c7',
    },
    {
        id: 'n8',
        type: 'ITEM_EXPIRED',
        title: 'Item listing expired',
        message: "Your listing 'Blue Umbrella' has expired after 30 days. You can repost it if the item is still missing.",
        isRead: true,
        createdAt: '2025-02-19T08:00:00',
        linkTo: '/my-items',
    },
    {
        id: 'n9',
        type: 'ITEM_MATCH',
        title: 'Possible match found',
        message: "A new lost item report 'Black Wallet with ID' was posted at CIT-U that may match an item you found.",
        isRead: true,
        createdAt: '2025-02-18T17:20:00',
        linkTo: '/items/10',
    },
    {
        id: 'n10',
        type: 'CLAIM_APPROVED',
        title: 'Claim approved — handover done',
        message: "Your claim on 'Prescription Glasses' was approved and the handover is complete. Thank you for using UniLost!",
        isRead: true,
        createdAt: '2025-02-17T13:45:00',
        linkTo: '/claims/c7',
    },
    {
        id: 'n11',
        type: 'CLAIM_RECEIVED',
        title: 'New claim on your item',
        message: "A student from USC claimed your 'TI-84 Calculator'. Review their details.",
        isRead: true,
        createdAt: '2025-02-16T10:00:00',
        linkTo: '/my-items/7/claims',
    },
];

export function getUnreadCount() {
    return mockNotifications.filter((n) => !n.isRead).length;
}

export function getRecentNotifications(limit = 5) {
    return mockNotifications.slice(0, limit);
}

export function markAsRead(id) {
    const notif = mockNotifications.find((n) => n.id === id);
    if (notif) notif.isRead = true;
}

export function markAllAsRead() {
    mockNotifications.forEach((n) => { n.isRead = true; });
}

export function timeAgo(dateString) {
    const now = new Date('2025-02-24T12:00:00');
    const date = new Date(dateString);
    const seconds = Math.floor((now - date) / 1000);

    if (seconds < 60) return 'just now';
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;
    const weeks = Math.floor(days / 7);
    return `${weeks}w ago`;
}
