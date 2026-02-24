package com.hulampay.mobile.data.mock

data class MockClaim(
    val id: String,
    val itemId: String,
    val itemTitle: String,
    val itemType: String,
    val itemImageUrl: String,
    val claimantId: String,
    val claimantName: String,
    val claimantSchool: String,
    val posterId: String,
    val posterName: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED", "HANDED_OVER"
    val secretDetailAnswer: String = "",
    val message: String,
    val createdAt: String,
    val posterConfirmed: Boolean = false,
    val claimantConfirmed: Boolean = false
)

object MockClaims {

    val claims = listOf(
        MockClaim(
            id = "c1", itemId = "1",
            itemTitle = "Black Samsung Galaxy S24", itemType = "LOST",
            itemImageUrl = "https://picsum.photos/seed/phone1/400/300",
            claimantId = "u2", claimantName = "Maria S.", claimantSchool = "USC",
            posterId = "u1", posterName = "Juan D.",
            status = "PENDING",
            message = "I think I saw this phone near the library. It was on the table where I was studying.",
            createdAt = "2025-02-22T09:15:00"
        ),
        MockClaim(
            id = "c2", itemId = "1",
            itemTitle = "Black Samsung Galaxy S24", itemType = "LOST",
            itemImageUrl = "https://picsum.photos/seed/phone1/400/300",
            claimantId = "u5", claimantName = "Miguel T.", claimantSchool = "UC",
            posterId = "u1", posterName = "Juan D.",
            status = "REJECTED",
            message = "This looks like my friend's phone. He lost a Samsung phone around the same time.",
            createdAt = "2025-02-21T16:30:00"
        ),
        MockClaim(
            id = "c3", itemId = "4",
            itemTitle = "Student ID Card - USJ-R", itemType = "FOUND",
            itemImageUrl = "https://picsum.photos/seed/id1/400/300",
            claimantId = "u1", claimantName = "Juan D.", claimantSchool = "CIT-U",
            posterId = "u4", posterName = "Ana G.",
            status = "APPROVED",
            secretDetailAnswer = "The ID has a small dent on the top-left corner and my student number ends in 2847.",
            message = "This is my ID card! I lost it when I visited USJ-R for an inter-school event.",
            createdAt = "2025-02-19T10:00:00",
            posterConfirmed = true, claimantConfirmed = false
        ),
        MockClaim(
            id = "c4", itemId = "6",
            itemTitle = "Set of Keys with Toyota Keychain", itemType = "FOUND",
            itemImageUrl = "https://picsum.photos/seed/keys1/400/300",
            claimantId = "u1", claimantName = "Juan D.", claimantSchool = "CIT-U",
            posterId = "u6", posterName = "Patricia L.",
            status = "PENDING",
            secretDetailAnswer = "One key has a blue rubber grip and there is a small Minion figurine keychain.",
            message = "These are my keys! I parked at SWU when visiting a friend.",
            createdAt = "2025-02-23T14:20:00"
        ),
        MockClaim(
            id = "c5", itemId = "4",
            itemTitle = "Student ID Card - USJ-R", itemType = "FOUND",
            itemImageUrl = "https://picsum.photos/seed/id1/400/300",
            claimantId = "u3", claimantName = "Carlo R.", claimantSchool = "UP Cebu",
            posterId = "u4", posterName = "Ana G.",
            status = "REJECTED",
            secretDetailAnswer = "It is a standard USJ-R ID with a blue lanyard.",
            message = "I think this might be my ID. I lost one recently.",
            createdAt = "2025-02-19T08:30:00"
        ),
        MockClaim(
            id = "c6", itemId = "9",
            itemTitle = "Prescription Eyeglasses", itemType = "LOST",
            itemImageUrl = "https://picsum.photos/seed/glasses1/400/300",
            claimantId = "u8", claimantName = "Daniel T.", claimantSchool = "CTU",
            posterId = "u8", posterName = "Daniel T.",
            status = "HANDED_OVER",
            message = "These are mine! The prescription is -2.50 for both eyes.",
            createdAt = "2025-02-16T09:00:00",
            posterConfirmed = true, claimantConfirmed = true
        ),
        MockClaim(
            id = "c7", itemId = "4",
            itemTitle = "Student ID Card - USJ-R", itemType = "FOUND",
            itemImageUrl = "https://picsum.photos/seed/id1/400/300",
            claimantId = "u7", claimantName = "Lea V.", claimantSchool = "CNU",
            posterId = "u4", posterName = "Ana G.",
            status = "PENDING",
            secretDetailAnswer = "The photo shows me wearing a white polo. My course is BS Nursing.",
            message = "I believe this is my ID. I visited USJ-R for a nursing seminar.",
            createdAt = "2025-02-20T11:45:00"
        )
    )

    fun getClaimsForItem(itemId: String): List<MockClaim> {
        return claims.filter { it.itemId == itemId }
    }

    fun getMyOutgoingClaims(userId: String): List<MockClaim> {
        return claims.filter { it.claimantId == userId }
    }

    fun getMyIncomingClaims(userId: String): List<MockClaim> {
        return claims.filter { it.posterId == userId }
    }

    fun getClaimById(claimId: String): MockClaim? {
        return claims.find { it.id == claimId }
    }
}
