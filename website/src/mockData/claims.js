// Mock data for Phase B - Claims & Handover UI
// All data is hardcoded. Will be replaced with API calls later.

import { timeAgo } from './items';

export const mockClaims = [
    {
        id: 'c1',
        itemId: '1',
        itemTitle: 'Black Samsung Galaxy S24',
        itemType: 'LOST',
        itemImageUrl: 'https://picsum.photos/seed/phone1/400/300',
        claimantId: 'u2',
        claimantName: 'Maria Santos',
        claimantSchool: 'USC',
        claimantEmail: 'maria.santos@usc.edu.ph',
        posterId: 'u1',
        posterName: 'Juan Dela Cruz',
        status: 'PENDING',
        secretDetailAnswer: '',
        message: 'I think I saw this phone near the library. It was on the table where I was studying. I can describe the wallpaper and apps on the home screen.',
        contactPreference: true,
        createdAt: '2025-02-22T09:15:00',
        handover: { posterConfirmed: false, claimantConfirmed: false },
    },
    {
        id: 'c2',
        itemId: '1',
        itemTitle: 'Black Samsung Galaxy S24',
        itemType: 'LOST',
        itemImageUrl: 'https://picsum.photos/seed/phone1/400/300',
        claimantId: 'u5',
        claimantName: 'Miguel Torres',
        claimantSchool: 'UC',
        claimantEmail: 'miguel.torres@uc.edu.ph',
        posterId: 'u1',
        posterName: 'Juan Dela Cruz',
        status: 'REJECTED',
        secretDetailAnswer: '',
        message: 'This looks like my friend\'s phone. He lost a Samsung phone around the same time at CIT-U.',
        contactPreference: false,
        createdAt: '2025-02-21T16:30:00',
        handover: { posterConfirmed: false, claimantConfirmed: false },
    },
    {
        id: 'c3',
        itemId: '4',
        itemTitle: 'Student ID Card - USJ-R',
        itemType: 'FOUND',
        itemImageUrl: 'https://picsum.photos/seed/id1/400/300',
        claimantId: 'u1',
        claimantName: 'Juan Dela Cruz',
        claimantSchool: 'CIT-U',
        claimantEmail: 'juan.delacruz@cit.edu',
        posterId: 'u4',
        posterName: 'Ana Garcia',
        status: 'APPROVED',
        secretDetailAnswer: 'The ID has a small dent on the top-left corner and my student number ends in 2847.',
        message: 'This is my ID card! I lost it when I visited USJ-R for an inter-school event last week.',
        contactPreference: true,
        createdAt: '2025-02-19T10:00:00',
        handover: { posterConfirmed: true, claimantConfirmed: false },
    },
    {
        id: 'c4',
        itemId: '6',
        itemTitle: 'Set of Keys with Toyota Keychain',
        itemType: 'FOUND',
        itemImageUrl: 'https://picsum.photos/seed/keys1/400/300',
        claimantId: 'u1',
        claimantName: 'Juan Dela Cruz',
        claimantSchool: 'CIT-U',
        claimantEmail: 'juan.delacruz@cit.edu',
        posterId: 'u6',
        posterName: 'Patricia Lim',
        status: 'PENDING',
        secretDetailAnswer: 'One of the keys has a blue rubber grip and there is a small Minion figurine keychain attached besides the Toyota one.',
        message: 'These are my keys! I parked at SWU when visiting a friend and must have dropped them.',
        contactPreference: true,
        createdAt: '2025-02-23T14:20:00',
        handover: { posterConfirmed: false, claimantConfirmed: false },
    },
    {
        id: 'c5',
        itemId: '4',
        itemTitle: 'Student ID Card - USJ-R',
        itemType: 'FOUND',
        itemImageUrl: 'https://picsum.photos/seed/id1/400/300',
        claimantId: 'u3',
        claimantName: 'Carlo Reyes',
        claimantSchool: 'UP Cebu',
        claimantEmail: 'carlo.reyes@up.edu.ph',
        posterId: 'u4',
        posterName: 'Ana Garcia',
        status: 'REJECTED',
        secretDetailAnswer: 'It is a standard USJ-R ID with a blue lanyard.',
        message: 'I think this might be my ID. I lost one recently.',
        contactPreference: false,
        createdAt: '2025-02-19T08:30:00',
        handover: { posterConfirmed: false, claimantConfirmed: false },
    },
    {
        id: 'c6',
        itemId: '9',
        itemTitle: 'Prescription Eyeglasses',
        itemType: 'LOST',
        itemImageUrl: 'https://picsum.photos/seed/glasses1/400/300',
        claimantId: 'u8',
        claimantName: 'Daniel Tan',
        claimantSchool: 'CTU',
        claimantEmail: 'daniel.tan@ctu.edu.ph',
        posterId: 'u8',
        posterName: 'Daniel Tan',
        status: 'HANDED_OVER',
        secretDetailAnswer: '',
        message: 'These are mine! The prescription is -2.50 for both eyes. Thank you for finding them!',
        contactPreference: true,
        createdAt: '2025-02-16T09:00:00',
        handover: { posterConfirmed: true, claimantConfirmed: true },
    },
    {
        id: 'c7',
        itemId: '4',
        itemTitle: 'Student ID Card - USJ-R',
        itemType: 'FOUND',
        itemImageUrl: 'https://picsum.photos/seed/id1/400/300',
        claimantId: 'u7',
        claimantName: 'Lea Villanueva',
        claimantSchool: 'CNU',
        claimantEmail: 'lea.villanueva@cnu.edu.ph',
        posterId: 'u4',
        posterName: 'Ana Garcia',
        status: 'PENDING',
        secretDetailAnswer: 'The photo on the ID shows me wearing a white polo. My course is BS Nursing.',
        message: 'Hi, I believe this is my ID. I visited USJ-R for a nursing seminar and may have dropped it near the gate.',
        contactPreference: true,
        createdAt: '2025-02-20T11:45:00',
        handover: { posterConfirmed: false, claimantConfirmed: false },
    },
];

// Helper: get all claims for a specific item
export function getClaimsForItem(itemId) {
    return mockClaims.filter((claim) => claim.itemId === itemId);
}

// Helper: get claims submitted BY the current user (outgoing)
export function getMyOutgoingClaims(userId) {
    return mockClaims.filter((claim) => claim.claimantId === userId);
}

// Helper: get claims submitted ON the current user's items (incoming)
export function getMyIncomingClaims(userId) {
    return mockClaims.filter((claim) => claim.posterId === userId);
}

// Helper: get a single claim by ID
export function getClaimById(claimId) {
    return mockClaims.find((claim) => claim.id === claimId);
}

// Re-export timeAgo for convenience
export { timeAgo };
