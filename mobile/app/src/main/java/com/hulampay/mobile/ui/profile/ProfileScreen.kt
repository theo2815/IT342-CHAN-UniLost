package com.hulampay.mobile.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Mock user data (to be replaced with real API data later)
    val firstName = "Theo"
    val lastName = "Chan"
    val email = "theo.chan@cit.edu"
    val studentId = "2023-12345"
    val school = "Cebu Institute of Technology - University"
    val schoolShort = "CIT-U"
    val role = "STUDENT"
    val karmaScore = 42
    val isAdmin = true // Mock: set to true for testing admin button

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Slate600, Slate700)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${firstName.first()}${lastName.first()}",
                                color = White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Verified badge
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF22c55e),
                            modifier = Modifier
                                .size(28.dp)
                                .offset(x = 2.dp, y = 2.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "$firstName $lastName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        school,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Member since 2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (role) {
                            "SUPER_ADMIN" -> Color(0xFFf59e0b).copy(alpha = 0.12f)
                            "ADMIN" -> Color(0xFFa855f7).copy(alpha = 0.12f)
                            else -> Color(0xFF3b82f6).copy(alpha = 0.12f)
                        }
                    ) {
                        Text(
                            text = when (role) {
                                "SUPER_ADMIN" -> "Super Admin"
                                "ADMIN" -> "Campus Admin"
                                else -> "Student"
                            },
                            color = when (role) {
                                "SUPER_ADMIN" -> Color(0xFFf59e0b)
                                "ADMIN" -> Color(0xFFa855f7)
                                else -> Color(0xFF3b82f6)
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Quick Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatCard("Items\nPosted", 5, Color(0xFF3b82f6), Modifier.weight(1f))
                ProfileStatCard("Claims\nMade", 3, Color(0xFFa855f7), Modifier.weight(1f))
                ProfileStatCard("Items\nRecovered", 2, Sage, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Student Info",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow(Icons.Outlined.Email, "Email", email)
                    InfoRow(Icons.Outlined.Badge, "Student ID", studentId)
                    InfoRow(Icons.Outlined.School, "School", "$schoolShort — $school")
                    InfoRow(Icons.Outlined.Star, "Karma Score", "$karmaScore pts")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.Settings.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate600)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }

                if (isAdmin) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Admin.route) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFa855f7))
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Panel")
                    }
                }

                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed,
                        containerColor = ErrorRed.copy(alpha = 0.04f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(ErrorRed.copy(alpha = 0.5f), ErrorRed.copy(alpha = 0.5f)))
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileStatCard(label: String, value: Int, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$value",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f),
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                label,
                fontSize = 12.sp,
                color = Slate400,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                fontSize = 14.sp,
                color = Slate800,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
