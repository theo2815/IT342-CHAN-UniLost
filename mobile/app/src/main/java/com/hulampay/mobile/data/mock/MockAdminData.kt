package com.hulampay.mobile.data.mock

data class MockAdminItem(
    val id: String,
    val type: String,
    val title: String,
    val status: String,
    val school: String,
    val postedBy: String,
    val postedByEmail: String
)

data class MockAdminUser(
    val id: String,
    val name: String,
    val email: String,
    val school: String,
    val role: String,
    val karmaScore: Int,
    val isBanned: Boolean
)

data class MockAdminStats(
    val activeItems: Int,
    val pendingClaims: Int,
    val bannedUsers: Int
)

object MockAdminData {
    val stats = MockAdminStats(
        activeItems = 47,
        pendingClaims = 12,
        bannedUsers = 2
    )

    val items = listOf(
        MockAdminItem("1", "LOST", "Black Samsung Galaxy S24", "ACTIVE", "CIT-U", "Juan Dela Cruz", "juan.delacruz@cit.edu"),
        MockAdminItem("2", "FOUND", "Student ID Card - Maria Santos", "CLAIMED", "USC", "Pedro Lim", "pedro.lim@uc.edu.ph"),
        MockAdminItem("3", "LOST", "Blue Umbrella with White Dots", "ACTIVE", "UP Cebu", "Ana Garcia", "ana.garcia@usjr.edu.ph"),
        MockAdminItem("4", "FOUND", "Silver MacBook Charger", "HANDED_OVER", "USJ-R", "Rosa Tan", "rosa.tan@swu.edu.ph"),
        MockAdminItem("5", "LOST", "Red Fjallraven Backpack", "ACTIVE", "UC", "Miguel Aquino", "miguel.aquino@cnu.edu.ph"),
        MockAdminItem("6", "FOUND", "Car Keys with Honda Keychain", "ACTIVE", "SWU", "Lisa Cruz", "lisa.cruz@ctu.edu.ph"),
        MockAdminItem("7", "LOST", "Calculus Textbook (Stewart 8th)", "EXPIRED", "CNU", "Sophia Mendoza", "sophia.mendoza@usc.edu.ph"),
        MockAdminItem("8", "FOUND", "Black Wallet with Cash", "ACTIVE", "CTU", "Daniel Villanueva", "daniel.v@up.edu.ph"),
        MockAdminItem("9", "LOST", "Prescription Glasses (Ray-Ban)", "CANCELLED", "CIT-U", "Juan Dela Cruz", "juan.delacruz@cit.edu"),
        MockAdminItem("10", "FOUND", "USB Flash Drive 64GB", "ACTIVE", "USC", "Maria Santos", "maria.santos@usc.edu.ph")
    )

    val users = listOf(
        MockAdminUser("u1", "Juan Dela Cruz", "juan.delacruz@cit.edu", "CIT-U", "STUDENT", 45, false),
        MockAdminUser("u2", "Maria Santos", "maria.santos@usc.edu.ph", "USC", "ADMIN", 120, false),
        MockAdminUser("u3", "Carlos Reyes", "carlos.reyes@up.edu.ph", "UP Cebu", "ADMIN", 98, false),
        MockAdminUser("u4", "Ana Garcia", "ana.garcia@usjr.edu.ph", "USJ-R", "STUDENT", 32, false),
        MockAdminUser("u5", "Pedro Lim", "pedro.lim@uc.edu.ph", "UC", "STUDENT", 15, true),
        MockAdminUser("u6", "Rosa Tan", "rosa.tan@swu.edu.ph", "SWU", "STUDENT", 67, false),
        MockAdminUser("u7", "Miguel Aquino", "miguel.aquino@cnu.edu.ph", "CNU", "STUDENT", 28, false),
        MockAdminUser("u8", "Lisa Cruz", "lisa.cruz@ctu.edu.ph", "CTU", "STUDENT", 53, false),
        MockAdminUser("u9", "John Spam", "john.spam@cit.edu", "CIT-U", "STUDENT", 0, true),
        MockAdminUser("u10", "Sophia Mendoza", "sophia.mendoza@usc.edu.ph", "USC", "STUDENT", 72, false),
        MockAdminUser("u11", "Daniel Villanueva", "daniel.v@up.edu.ph", "UP Cebu", "STUDENT", 41, false),
        MockAdminUser("u12", "Admin Super", "admin@unilost.com", "UniLost", "SUPER_ADMIN", 999, false)
    )
}
