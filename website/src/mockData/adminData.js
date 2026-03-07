// Mock data for Phase D - Admin Panels
// All admin data is hardcoded. Will be replaced with API calls later.

// ── Time helper ──────────────────────────────────────────────
export const timeAgo = (dateString) => {
    const now = new Date('2025-02-24T12:00:00');
    const date = new Date(dateString);
    const seconds = Math.floor((now - date) / 1000);
    if (seconds < 60) return 'just now';
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 30) return `${days}d ago`;
    const months = Math.floor(days / 30);
    return `${months}mo ago`;
};

// ── Dashboard Stats ──────────────────────────────────────────
export const mockAdminStats = {
    totalUsers: 156,
    activeItems: 47,
    pendingClaims: 12,
    recoveredThisMonth: 8,
    bannedUsers: 2,
    totalSchools: 8,
};

// ── Recent Admin Actions ─────────────────────────────────────
export const mockAdminActions = [
    { id: 'a1', action: 'Removed item', target: 'Fake Lost AirPods listing', admin: 'Admin Maria', timestamp: '2025-02-24T10:30:00', type: 'ITEM_REMOVAL' },
    { id: 'a2', action: 'Banned user', target: 'john.spam@cit.edu', admin: 'Admin Maria', timestamp: '2025-02-24T09:15:00', type: 'USER_BAN' },
    { id: 'a3', action: 'Override handover', target: 'Claim #c5 on Student ID Card', admin: 'Admin Carlos', timestamp: '2025-02-23T16:45:00', type: 'CLAIM_OVERRIDE' },
    { id: 'a4', action: 'Unbanned user', target: 'mark.reyes@usc.edu.ph', admin: 'Admin Maria', timestamp: '2025-02-23T14:20:00', type: 'USER_UNBAN' },
    { id: 'a5', action: 'Removed item', target: 'Inappropriate listing reported', admin: 'Admin Carlos', timestamp: '2025-02-22T11:00:00', type: 'ITEM_REMOVAL' },
    { id: 'a6', action: 'Updated school', target: 'CIT-U email domain updated', admin: 'Super Admin', timestamp: '2025-02-22T09:30:00', type: 'SCHOOL_UPDATE' },
    { id: 'a7', action: 'Banned user', target: 'spam.account@uc.edu.ph', admin: 'Admin Maria', timestamp: '2025-02-21T15:10:00', type: 'USER_BAN' },
    { id: 'a8', action: 'Override handover', target: 'Claim #c8 on Blue Umbrella', admin: 'Admin Carlos', timestamp: '2025-02-20T10:00:00', type: 'CLAIM_OVERRIDE' },
];

// ── Admin Users ──────────────────────────────────────────────
export const mockAdminUsers = [
    { id: 'u1', fullName: 'Juan Dela Cruz', email: 'juan.delacruz@cit.edu', campus: { id: 'CIT-U-MAIN', name: 'Cebu Institute of Technology - University' }, role: 'STUDENT', karmaScore: 45, isBanned: false, createdAt: '2024-06-15T08:00:00' },
    { id: 'u2', fullName: 'Maria Santos', email: 'maria.santos@usc.edu.ph', campus: { id: 'USC-MAIN', name: 'University of San Carlos' }, role: 'ADMIN', karmaScore: 120, isBanned: false, createdAt: '2024-05-10T08:00:00' },
    { id: 'u3', fullName: 'Carlos Reyes', email: 'carlos.reyes@up.edu.ph', campus: { id: 'UP-CEBU', name: 'University of the Philippines Cebu' }, role: 'ADMIN', karmaScore: 98, isBanned: false, createdAt: '2024-05-12T08:00:00' },
    { id: 'u4', fullName: 'Ana Garcia', email: 'ana.garcia@usjr.edu.ph', campus: { id: 'USJR-MAIN', name: 'University of San Jose-Recoletos' }, role: 'STUDENT', karmaScore: 32, isBanned: false, createdAt: '2024-08-20T08:00:00' },
    { id: 'u5', fullName: 'Pedro Lim', email: 'pedro.lim@uc.edu.ph', campus: { id: 'UC-MAIN', name: 'University of Cebu' }, role: 'STUDENT', karmaScore: 15, isBanned: true, createdAt: '2024-07-01T08:00:00' },
    { id: 'u6', fullName: 'Rosa Tan', email: 'rosa.tan@swu.edu.ph', campus: { id: 'SWU-MAIN', name: 'Southwestern University PHINMA' }, role: 'STUDENT', karmaScore: 67, isBanned: false, createdAt: '2024-09-15T08:00:00' },
    { id: 'u7', fullName: 'Miguel Aquino', email: 'miguel.aquino@cnu.edu.ph', campus: { id: 'CNU-MAIN', name: 'Cebu Normal University' }, role: 'STUDENT', karmaScore: 28, isBanned: false, createdAt: '2024-10-05T08:00:00' },
    { id: 'u8', fullName: 'Lisa Cruz', email: 'lisa.cruz@ctu.edu.ph', campus: { id: 'CTU-MAIN', name: 'Cebu Technological University' }, role: 'STUDENT', karmaScore: 53, isBanned: false, createdAt: '2024-08-12T08:00:00' },
    { id: 'u9', fullName: 'John Spam', email: 'john.spam@cit.edu', campus: { id: 'CIT-U-MAIN', name: 'Cebu Institute of Technology - University' }, role: 'STUDENT', karmaScore: 0, isBanned: true, createdAt: '2025-01-10T08:00:00' },
    { id: 'u10', fullName: 'Sophia Mendoza', email: 'sophia.mendoza@usc.edu.ph', campus: { id: 'USC-MAIN', name: 'University of San Carlos' }, role: 'STUDENT', karmaScore: 72, isBanned: false, createdAt: '2024-06-20T08:00:00' },
    { id: 'u11', fullName: 'Daniel Villanueva', email: 'daniel.v@up.edu.ph', campus: { id: 'UP-CEBU', name: 'University of the Philippines Cebu' }, role: 'STUDENT', karmaScore: 41, isBanned: false, createdAt: '2024-11-01T08:00:00' },
    { id: 'u12', fullName: 'UniLost Admin', email: 'admin@cit.edu', campus: { id: 'CIT-U-MAIN', name: 'Cebu Institute of Technology - University' }, role: 'ADMIN', karmaScore: 999, isBanned: false, createdAt: '2024-01-01T08:00:00' },
];

// ── Admin Items ──────────────────────────────────────────────
export const mockAdminItems = [
    { id: '1', type: 'LOST', title: 'Black Samsung Galaxy S24', status: 'ACTIVE', school: { shortName: 'CIT-U' }, postedBy: { firstName: 'Juan', lastName: 'Dela Cruz', email: 'juan.delacruz@cit.edu' }, imageUrl: 'https://picsum.photos/seed/item1/400/300', createdAt: '2025-02-20T10:30:00', claimCount: 2 },
    { id: '2', type: 'FOUND', title: 'Student ID Card - Maria Santos', status: 'CLAIMED', school: { shortName: 'USC' }, postedBy: { firstName: 'Pedro', lastName: 'Lim', email: 'pedro.lim@uc.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item2/400/300', createdAt: '2025-02-19T14:15:00', claimCount: 3 },
    { id: '3', type: 'LOST', title: 'Blue Umbrella with White Dots', status: 'ACTIVE', school: { shortName: 'UP Cebu' }, postedBy: { firstName: 'Ana', lastName: 'Garcia', email: 'ana.garcia@usjr.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item3/400/300', createdAt: '2025-02-18T09:00:00', claimCount: 0 },
    { id: '4', type: 'FOUND', title: 'Silver MacBook Charger', status: 'HANDED_OVER', school: { shortName: 'USJ-R' }, postedBy: { firstName: 'Rosa', lastName: 'Tan', email: 'rosa.tan@swu.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item4/400/300', createdAt: '2025-02-17T16:30:00', claimCount: 1 },
    { id: '5', type: 'LOST', title: 'Red Fjallraven Backpack', status: 'ACTIVE', school: { shortName: 'UC' }, postedBy: { firstName: 'Miguel', lastName: 'Aquino', email: 'miguel.aquino@cnu.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item5/400/300', createdAt: '2025-02-16T11:45:00', claimCount: 1 },
    { id: '6', type: 'FOUND', title: 'Car Keys with Honda Keychain', status: 'ACTIVE', school: { shortName: 'SWU' }, postedBy: { firstName: 'Lisa', lastName: 'Cruz', email: 'lisa.cruz@ctu.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item6/400/300', createdAt: '2025-02-15T13:20:00', claimCount: 0 },
    { id: '7', type: 'LOST', title: 'Calculus Textbook (Stewart 8th)', status: 'EXPIRED', school: { shortName: 'CNU' }, postedBy: { firstName: 'Sophia', lastName: 'Mendoza', email: 'sophia.mendoza@usc.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item7/400/300', createdAt: '2025-01-20T08:00:00', claimCount: 0 },
    { id: '8', type: 'FOUND', title: 'Black Wallet with Cash', status: 'ACTIVE', school: { shortName: 'CTU' }, postedBy: { firstName: 'Daniel', lastName: 'Villanueva', email: 'daniel.v@up.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item8/400/300', createdAt: '2025-02-14T10:00:00', claimCount: 4 },
    { id: '9', type: 'LOST', title: 'Prescription Glasses (Ray-Ban)', status: 'CANCELLED', school: { shortName: 'CIT-U' }, postedBy: { firstName: 'Juan', lastName: 'Dela Cruz', email: 'juan.delacruz@cit.edu' }, imageUrl: 'https://picsum.photos/seed/item9/400/300', createdAt: '2025-02-10T09:30:00', claimCount: 0 },
    { id: '10', type: 'FOUND', title: 'USB Flash Drive 64GB', status: 'ACTIVE', school: { shortName: 'USC' }, postedBy: { firstName: 'Maria', lastName: 'Santos', email: 'maria.santos@usc.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item10/400/300', createdAt: '2025-02-22T15:00:00', claimCount: 1 },
    { id: '11', type: 'LOST', title: 'Gold Necklace with Cross Pendant', status: 'ACTIVE', school: { shortName: 'USJ-R' }, postedBy: { firstName: 'Ana', lastName: 'Garcia', email: 'ana.garcia@usjr.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item11/400/300', createdAt: '2025-02-23T08:00:00', claimCount: 0 },
    { id: '12', type: 'FOUND', title: 'Gray Nike Hoodie Size M', status: 'CLAIMED', school: { shortName: 'UP Cebu' }, postedBy: { firstName: 'Carlos', lastName: 'Reyes', email: 'carlos.reyes@up.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item12/400/300', createdAt: '2025-02-21T12:00:00', claimCount: 2 },
    { id: '13', type: 'LOST', title: 'Mechanical Keyboard (Keychron K2)', status: 'ACTIVE', school: { shortName: 'CIT-U' }, postedBy: { firstName: 'Daniel', lastName: 'Villanueva', email: 'daniel.v@up.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item13/400/300', createdAt: '2025-02-24T07:30:00', claimCount: 0 },
    { id: '14', type: 'FOUND', title: 'Stainless Steel Water Bottle', status: 'ACTIVE', school: { shortName: 'SWU' }, postedBy: { firstName: 'Rosa', lastName: 'Tan', email: 'rosa.tan@swu.edu.ph' }, imageUrl: 'https://picsum.photos/seed/item14/400/300', createdAt: '2025-02-23T14:00:00', claimCount: 0 },
    { id: '15', type: 'LOST', title: 'Fake Lost AirPods (REMOVED)', status: 'CANCELLED', school: { shortName: 'UC' }, postedBy: { firstName: 'John', lastName: 'Spam', email: 'john.spam@cit.edu' }, imageUrl: 'https://picsum.photos/seed/item15/400/300', createdAt: '2025-02-24T06:00:00', claimCount: 0 },
];

// ── Admin Claims ─────────────────────────────────────────────
export const mockAdminClaims = [
    { id: 'c1', itemId: '1', itemTitle: 'Black Samsung Galaxy S24', itemType: 'LOST', itemImageUrl: 'https://picsum.photos/seed/item1/400/300', claimantName: 'Ana Garcia', claimantEmail: 'ana.garcia@usjr.edu.ph', posterName: 'Juan Dela Cruz', status: 'PENDING', createdAt: '2025-02-22T09:15:00' },
    { id: 'c2', itemId: '1', itemTitle: 'Black Samsung Galaxy S24', itemType: 'LOST', itemImageUrl: 'https://picsum.photos/seed/item1/400/300', claimantName: 'Pedro Lim', claimantEmail: 'pedro.lim@uc.edu.ph', posterName: 'Juan Dela Cruz', status: 'PENDING', createdAt: '2025-02-22T14:30:00' },
    { id: 'c3', itemId: '2', itemTitle: 'Student ID Card - Maria Santos', itemType: 'FOUND', itemImageUrl: 'https://picsum.photos/seed/item2/400/300', claimantName: 'Maria Santos', claimantEmail: 'maria.santos@usc.edu.ph', posterName: 'Pedro Lim', status: 'APPROVED', createdAt: '2025-02-20T10:00:00' },
    { id: 'c4', itemId: '2', itemTitle: 'Student ID Card - Maria Santos', itemType: 'FOUND', itemImageUrl: 'https://picsum.photos/seed/item2/400/300', claimantName: 'Rosa Tan', claimantEmail: 'rosa.tan@swu.edu.ph', posterName: 'Pedro Lim', status: 'REJECTED', createdAt: '2025-02-20T11:00:00' },
    { id: 'c5', itemId: '4', itemTitle: 'Silver MacBook Charger', itemType: 'FOUND', itemImageUrl: 'https://picsum.photos/seed/item4/400/300', claimantName: 'Sophia Mendoza', claimantEmail: 'sophia.mendoza@usc.edu.ph', posterName: 'Rosa Tan', status: 'HANDED_OVER', createdAt: '2025-02-18T09:00:00' },
    { id: 'c6', itemId: '5', itemTitle: 'Red Fjallraven Backpack', itemType: 'LOST', itemImageUrl: 'https://picsum.photos/seed/item5/400/300', claimantName: 'Lisa Cruz', claimantEmail: 'lisa.cruz@ctu.edu.ph', posterName: 'Miguel Aquino', status: 'PENDING', createdAt: '2025-02-23T16:00:00' },
    { id: 'c7', itemId: '8', itemTitle: 'Black Wallet with Cash', itemType: 'FOUND', itemImageUrl: 'https://picsum.photos/seed/item8/400/300', claimantName: 'Juan Dela Cruz', claimantEmail: 'juan.delacruz@cit.edu', posterName: 'Daniel Villanueva', status: 'APPROVED', createdAt: '2025-02-21T08:00:00' },
    { id: 'c8', itemId: '8', itemTitle: 'Black Wallet with Cash', itemType: 'FOUND', itemImageUrl: 'https://picsum.photos/seed/item8/400/300', claimantName: 'Miguel Aquino', claimantEmail: 'miguel.aquino@cnu.edu.ph', posterName: 'Daniel Villanueva', status: 'REJECTED', createdAt: '2025-02-21T09:00:00' },
    { id: 'c9', itemId: '10', itemTitle: 'USB Flash Drive 64GB', itemType: 'FOUND', itemImageUrl: 'https://picsum.photos/seed/item10/400/300', claimantName: 'Daniel Villanueva', claimantEmail: 'daniel.v@up.edu.ph', posterName: 'Maria Santos', status: 'PENDING', createdAt: '2025-02-24T08:00:00' },
    { id: 'c10', itemId: '12', itemTitle: 'Gray Nike Hoodie Size M', itemType: 'FOUND', itemImageUrl: 'https://picsum.photos/seed/item12/400/300', claimantName: 'Lisa Cruz', claimantEmail: 'lisa.cruz@ctu.edu.ph', posterName: 'Carlos Reyes', status: 'APPROVED', createdAt: '2025-02-22T10:00:00' },
];

// ── Schools (for Super Admin) ────────────────────────────────
export const mockSchools = [
    { id: '1', name: 'Cebu Institute of Technology - University', shortName: 'CIT-U', emailDomain: 'cit.edu', isActive: true, studentCount: 28, itemCount: 12 },
    { id: '2', name: 'University of San Carlos', shortName: 'USC', emailDomain: 'usc.edu.ph', isActive: true, studentCount: 24, itemCount: 9 },
    { id: '3', name: 'University of the Philippines Cebu', shortName: 'UP Cebu', emailDomain: 'up.edu.ph', isActive: true, studentCount: 18, itemCount: 7 },
    { id: '4', name: 'University of San Jose-Recoletos', shortName: 'USJ-R', emailDomain: 'usjr.edu.ph', isActive: true, studentCount: 22, itemCount: 6 },
    { id: '5', name: 'University of Cebu', shortName: 'UC', emailDomain: 'uc.edu.ph', isActive: true, studentCount: 20, itemCount: 5 },
    { id: '6', name: 'Southwestern University PHINMA', shortName: 'SWU', emailDomain: 'swu.edu.ph', isActive: true, studentCount: 16, itemCount: 4 },
    { id: '7', name: 'Cebu Normal University', shortName: 'CNU', emailDomain: 'cnu.edu.ph', isActive: true, studentCount: 15, itemCount: 2 },
    { id: '8', name: 'Cebu Technological University', shortName: 'CTU', emailDomain: 'ctu.edu.ph', isActive: true, studentCount: 13, itemCount: 2 },
];

// ── Campus Stats (for Super Admin cross-campus view) ─────────
export const mockCampusStats = mockSchools.map(school => ({
    ...school,
    recoveredItems: Math.floor(Math.random() * school.itemCount),
    recoveryRate: Math.floor(40 + Math.random() * 50),
    pendingClaims: Math.floor(Math.random() * 5),
}));
