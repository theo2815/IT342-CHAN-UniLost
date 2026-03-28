package com.hulampay.mobile.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockAdminData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Items", "Users")

    // Remove/ban state
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf("") }
    var confirmTargetName by remember { mutableStateOf("") }
    var confirmReason by remember { mutableStateOf("") }

    // Mutable lists for mock interactions
    var items by remember { mutableStateOf(MockAdminData.items) }
    var users by remember { mutableStateOf(MockAdminData.users) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                item {
                    StatMiniCard(
                        label = "Active Items",
                        value = "${MockAdminData.stats.activeItems}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    StatMiniCard(
                        label = "Pending Claims",
                        value = "${MockAdminData.stats.pendingClaims}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    StatMiniCard(
                        label = "Banned Users",
                        value = "${MockAdminData.stats.bannedUsers}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> {
                    // Items Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            AdminItemRow(
                                title = item.title,
                                type = item.type,
                                school = item.school,
                                status = item.status,
                                onRemove = {
                                    confirmAction = "remove"
                                    confirmTargetName = item.title
                                    confirmReason = ""
                                    showConfirmDialog = true
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // Users Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users) { user ->
                            AdminUserRow(
                                name = user.name,
                                email = user.email,
                                role = user.role,
                                isBanned = user.isBanned,
                                onToggleBan = {
                                    confirmAction = if (user.isBanned) "unban" else "ban"
                                    confirmTargetName = user.name
                                    confirmReason = ""
                                    showConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Confirm Dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        when (confirmAction) {
                            "remove" -> "Remove Item"
                            "ban" -> "Ban User"
                            "unban" -> "Unban User"
                            else -> "Confirm"
                        }
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            when (confirmAction) {
                                "remove" -> "Remove \"$confirmTargetName\"?"
                                "ban" -> "Ban $confirmTargetName?"
                                "unban" -> "Unban $confirmTargetName?"
                                else -> "Are you sure?"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = confirmReason,
                            onValueChange = { confirmReason = it },
                            label = { Text("Reason") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            // Mock action — in real app, call API
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (confirmAction == "unban") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatMiniCard(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminItemRow(
    title: String,
    type: String,
    school: String,
    status: String,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(type, fontSize = 10.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (type == "LOST")
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            labelColor = if (type == "LOST")
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                    Text(
                        text = "$school  |  $status",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AdminUserRow(
    name: String,
    email: String,
    role: String,
    isBanned: Boolean,
    onToggleBan: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isBanned) {
                        Text(
                            text = "BANNED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(
                    text = email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (role) {
                        "ADMIN" -> "Campus Admin"
                        "SUPER_ADMIN" -> "Super Admin"
                        else -> "Student"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (role) {
                        "ADMIN" -> MaterialTheme.colorScheme.tertiary
                        "SUPER_ADMIN" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Only allow ban/unban for students
            if (role == "STUDENT") {
                IconButton(onClick = onToggleBan) {
                    Icon(
                        if (isBanned) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = if (isBanned) "Unban" else "Ban",
                        tint = if (isBanned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
