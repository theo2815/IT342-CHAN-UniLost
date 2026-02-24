package com.hulampay.mobile.data.mock

data class MockNotification(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val linkRoute: String
)

object MockNotifications {
    val notifications = listOf(
        MockNotification(
            id = "n1",
            type = "CLAIM_RECEIVED",
            title = "New claim on your item",
            message = "Maria Santos claimed your 'Black Samsung Galaxy S24'. Review their claim to verify ownership.",
            isRead = false,
            createdAt = "2025-02-24T08:30:00",
            linkRoute = "item_detail_screen/1"
        ),
        MockNotification(
            id = "n2",
            type = "CLAIM_APPROVED",
            title = "Your claim was approved!",
            message = "Great news! Your claim on 'Student ID Card - USJ-R' was approved. Please arrange a handover.",
            isRead = false,
            createdAt = "2025-02-23T15:30:00",
            linkRoute = "claim_detail_screen/c3"
        ),
        MockNotification(
            id = "n3",
            type = "HANDOVER_REMINDER",
            title = "Complete your handover",
            message = "Reminder: You have a pending handover for 'Student ID Card - USJ-R'. Meet at the campus security office.",
            isRead = false,
            createdAt = "2025-02-23T10:00:00",
            linkRoute = "claim_detail_screen/c3"
        ),
        MockNotification(
            id = "n4",
            type = "CLAIM_RECEIVED",
            title = "New claim on your item",
            message = "Carlos Reyes claimed your 'Black Samsung Galaxy S24'. Check their secret detail answer.",
            isRead = false,
            createdAt = "2025-02-22T16:45:00",
            linkRoute = "item_detail_screen/1"
        ),
        MockNotification(
            id = "n5",
            type = "ITEM_MATCH",
            title = "Possible match found",
            message = "A new found item 'Blue Hydroflask' was posted at UC Main that may match your lost report.",
            isRead = true,
            createdAt = "2025-02-22T09:15:00",
            linkRoute = "item_detail_screen/5"
        ),
        MockNotification(
            id = "n6",
            type = "CLAIM_REJECTED",
            title = "Claim not approved",
            message = "Your claim on 'Red Nike Backpack' was not approved by the poster. You can browse other items.",
            isRead = true,
            createdAt = "2025-02-21T14:00:00",
            linkRoute = "claim_detail_screen/c5"
        ),
        MockNotification(
            id = "n7",
            type = "HANDOVER_CONFIRMED",
            title = "Handover complete!",
            message = "The handover for 'Calculator (Casio fx-991)' has been confirmed by both parties. Glad we could help!",
            isRead = true,
            createdAt = "2025-02-20T11:30:00",
            linkRoute = "claim_detail_screen/c7"
        ),
        MockNotification(
            id = "n8",
            type = "ITEM_EXPIRED",
            title = "Item listing expired",
            message = "Your listing 'Blue Umbrella' has expired after 30 days. You can repost it if the item is still missing.",
            isRead = true,
            createdAt = "2025-02-19T08:00:00",
            linkRoute = "my_items_screen"
        ),
        MockNotification(
            id = "n9",
            type = "ITEM_MATCH",
            title = "Possible match found",
            message = "A new lost item report 'Black Wallet with ID' was posted at CIT-U that may match an item you found.",
            isRead = true,
            createdAt = "2025-02-18T17:20:00",
            linkRoute = "item_detail_screen/10"
        ),
        MockNotification(
            id = "n10",
            type = "CLAIM_APPROVED",
            title = "Claim approved — handover done",
            message = "Your claim on 'Prescription Glasses' was approved and the handover is complete. Thank you for using UniLost!",
            isRead = true,
            createdAt = "2025-02-17T13:45:00",
            linkRoute = "claim_detail_screen/c7"
        ),
        MockNotification(
            id = "n11",
            type = "CLAIM_RECEIVED",
            title = "New claim on your item",
            message = "A student from USC claimed your 'TI-84 Calculator'. Review their details.",
            isRead = true,
            createdAt = "2025-02-16T10:00:00",
            linkRoute = "item_detail_screen/7"
        )
    )

    fun getUnreadCount(): Int = notifications.count { !it.isRead }

    fun getAll(): List<MockNotification> = notifications

    fun timeAgo(dateString: String): String {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val now = sdf.parse("2025-02-24T12:00:00") ?: return dateString
            val date = sdf.parse(dateString) ?: return dateString
            val seconds = (now.time - date.time) / 1000

            return when {
                seconds < 60 -> "just now"
                seconds < 3600 -> "${seconds / 60}m ago"
                seconds < 86400 -> "${seconds / 3600}h ago"
                seconds < 604800 -> "${seconds / 86400}d ago"
                else -> "${seconds / 604800}w ago"
            }
        } catch (e: Exception) {
            return dateString
        }
    }
}
