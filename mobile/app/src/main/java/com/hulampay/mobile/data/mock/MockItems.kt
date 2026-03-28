package com.hulampay.mobile.data.mock

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class MockItem(
    val id: String,
    val type: String, // "LOST" or "FOUND"
    val title: String,
    val description: String,
    val category: String,
    val status: String, // "ACTIVE", "CLAIMED", "HANDED_OVER", "EXPIRED", "CANCELLED"
    val imageUrl: String,
    val postedByName: String,
    val postedBySchool: String,
    val schoolName: String,
    val schoolShortName: String,
    val locationDescription: String,
    val createdAt: String, // ISO date string
    val claimCount: Int = 0,
    val secretDetail: String? = null,
)

object MockItems {

    val categories = listOf(
        "Electronics", "Documents", "Clothing", "Accessories",
        "Books", "Bags", "Keys", "Wallets", "Other"
    )

    val schools = listOf(
        "CIT-U", "USC", "UP Cebu", "USJ-R", "UC", "SWU", "CNU", "CTU"
    )

    val items = listOf(
        MockItem(
            id = "1", type = "LOST",
            title = "Black Samsung Galaxy S24",
            description = "Lost near CIT-U library, 2nd floor. Dark blue case with a small crack on the top right corner.",
            category = "Electronics", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/phone1/400/300",
            postedByName = "Juan D.", postedBySchool = "CIT-U",
            schoolName = "Cebu Institute of Technology - University", schoolShortName = "CIT-U",
            locationDescription = "CIT-U Main Library, 2nd Floor",
            createdAt = "2025-02-20T10:30:00", claimCount = 2
        ),
        MockItem(
            id = "2", type = "FOUND",
            title = "Blue Umbrella with Wooden Handle",
            description = "Found left behind in Room 301 after the 4PM class. Navy blue with curved wooden handle.",
            category = "Accessories", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/umbrella1/400/300",
            postedByName = "Maria S.", postedBySchool = "USC",
            schoolName = "University of San Carlos", schoolShortName = "USC",
            locationDescription = "USC Bunzel Building, Room 301",
            createdAt = "2025-02-21T14:15:00"
        ),
        MockItem(
            id = "3", type = "LOST",
            title = "Silver MacBook Pro 14\"",
            description = "Left in computer lab. Has a 'UP Fighting Maroons' sticker on the back.",
            category = "Electronics", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/laptop1/400/300",
            postedByName = "Carlo R.", postedBySchool = "UP Cebu",
            schoolName = "University of the Philippines Cebu", schoolShortName = "UP Cebu",
            locationDescription = "UP Cebu Computer Lab, Room 204",
            createdAt = "2025-02-19T09:00:00", claimCount = 1
        ),
        MockItem(
            id = "4", type = "FOUND",
            title = "Student ID Card - USJ-R",
            description = "Found on the ground near the main gate. The card looks relatively new.",
            category = "Documents", status = "CLAIMED",
            imageUrl = "https://picsum.photos/seed/id1/400/300",
            postedByName = "Ana G.", postedBySchool = "USJ-R",
            schoolName = "University of San Jose-Recoletos", schoolShortName = "USJ-R",
            locationDescription = "USJ-R Basak Campus Main Gate",
            createdAt = "2025-02-18T16:45:00", claimCount = 3
        ),
        MockItem(
            id = "5", type = "LOST",
            title = "Red JanSport Backpack",
            description = "Left in the cafeteria. Has notebooks, water bottle, and a cat keychain on the zipper.",
            category = "Bags", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/bag1/400/300",
            postedByName = "Miguel T.", postedBySchool = "UC",
            schoolName = "University of Cebu", schoolShortName = "UC",
            locationDescription = "UC Main Campus Cafeteria",
            createdAt = "2025-02-22T12:00:00"
        ),
        MockItem(
            id = "6", type = "FOUND",
            title = "Set of Keys with Toyota Keychain",
            description = "Found near parking area. 4 keys on a ring with Toyota logo keychain.",
            category = "Keys", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/keys1/400/300",
            postedByName = "Patricia L.", postedBySchool = "SWU",
            schoolName = "Southwestern University PHINMA", schoolShortName = "SWU",
            locationDescription = "SWU Parking Area B",
            createdAt = "2025-02-23T08:30:00", claimCount = 1
        ),
        MockItem(
            id = "7", type = "LOST",
            title = "Brown Leather Wallet",
            description = "Lost between Engineering building and gym. Herschel brand, contains ID and cash.",
            category = "Wallets", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/wallet1/400/300",
            postedByName = "Juan D.", postedBySchool = "CIT-U",
            schoolName = "Cebu Institute of Technology - University", schoolShortName = "CIT-U",
            locationDescription = "CIT-U Engineering to Gym pathway",
            createdAt = "2025-02-23T15:20:00"
        ),
        MockItem(
            id = "8", type = "FOUND",
            title = "TI-84 Plus Calculator",
            description = "Found in Room 105 after Math exam. Has a partially faded name on the back.",
            category = "Electronics", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/calc1/400/300",
            postedByName = "Lea V.", postedBySchool = "CNU",
            schoolName = "Cebu Normal University", schoolShortName = "CNU",
            locationDescription = "CNU Room 105",
            createdAt = "2025-02-22T17:00:00"
        ),
        MockItem(
            id = "9", type = "LOST",
            title = "Prescription Eyeglasses",
            description = "Dark tortoiseshell frames in a black hard case. Lost in main building hallway.",
            category = "Accessories", status = "HANDED_OVER",
            imageUrl = "https://picsum.photos/seed/glasses1/400/300",
            postedByName = "Daniel T.", postedBySchool = "CTU",
            schoolName = "Cebu Technological University", schoolShortName = "CTU",
            locationDescription = "CTU Main Building Hallway",
            createdAt = "2025-02-15T11:00:00", claimCount = 1
        ),
        MockItem(
            id = "10", type = "FOUND",
            title = "Nursing Textbook - Fundamentals",
            description = "Kozier & Erb, 11th edition, found on bench near chapel. Has highlighted pages.",
            category = "Books", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/book1/400/300",
            postedByName = "Patricia L.", postedBySchool = "SWU",
            schoolName = "Southwestern University PHINMA", schoolShortName = "SWU",
            locationDescription = "SWU Chapel Area Bench",
            createdAt = "2025-02-21T09:45:00"
        ),
        MockItem(
            id = "11", type = "FOUND",
            title = "USB Flash Drive 64GB",
            description = "Black SanDisk 64GB USB found plugged into library computer station 8.",
            category = "Electronics", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/usb1/400/300",
            postedByName = "Carlo R.", postedBySchool = "UP Cebu",
            schoolName = "University of the Philippines Cebu", schoolShortName = "UP Cebu",
            locationDescription = "UP Cebu Library, Station 8",
            createdAt = "2025-02-24T10:00:00"
        ),
        MockItem(
            id = "12", type = "LOST",
            title = "AirPods Pro (2nd Gen)",
            description = "White charging case with small scratch on lid. Last had them in College of Arts.",
            category = "Electronics", status = "ACTIVE",
            imageUrl = "https://picsum.photos/seed/airpods1/400/300",
            postedByName = "Lea V.", postedBySchool = "CNU",
            schoolName = "Cebu Normal University", schoolShortName = "CNU",
            locationDescription = "CNU College of Arts Building",
            createdAt = "2025-02-23T13:30:00"
        )
    )

    fun timeAgo(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return dateString
            val now = Date()
            val diff = now.time - date.time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                days < 7 -> "${days}d ago"
                days < 30 -> "${days / 7}w ago"
                else -> "${days / 30}mo ago"
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
