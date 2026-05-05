package com.hulampay.mobile.ui.items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockAdminData
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
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
            UniLostDetailTopBar(
                title = "Admin Panel",
                onBackClick = { navController.popBackStack() }
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
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                modifier = Modifier.padding(vertical = UniLostSpacing.sm)
            ) {
                item {
                    AdminStatCard(
                        label = "Active Items",
                        value = "${MockAdminData.stats.activeItems}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    AdminStatCard(
                        label = "Pending Claims",
                        value = "${MockAdminData.stats.pendingClaims}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    AdminStatCard(
                        label = "Banned Users",
                        value = "${MockAdminData.stats.bannedUsers}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 0.5.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> {
                    // Items Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(UniLostSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
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
                        contentPadding = PaddingValues(UniLostSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
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
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)) {
                        Text(
                            when (confirmAction) {
                                "remove" -> "Remove \"$confirmTargetName\"?"
                                "ban" -> "Ban $confirmTargetName?"
                                "unban" -> "Unban $confirmTargetName?"
                                else -> "Are you sure?"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        UniLostTextField(
                            value = confirmReason,
                            onValueChange = { confirmReason = it },
                            label = "Reason",
                            singleLine = false,
                            minLines = 2
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
                            contentColor = if (confirmAction == "unban") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    ) {
                        Text(
                            "Confirm",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                shape = UniLostShapes.lg
            )
        }
    }
}

@Composable
private fun AdminStatCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(UniLostSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
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
        shape = UniLostShapes.md,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(type)
                    Text(
                        text = "$school  |  $status",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
        shape = UniLostShapes.md,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AvatarView(name = name, size = 40.dp)

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isBanned) {
                        StatusChip("BANNED", ErrorRed)
                    }
                }
                Text(
                    text = email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusChip(role)
            }

            // Only allow ban/unban for students
            if (role == "STUDENT") {
                IconButton(onClick = onToggleBan) {
                    Icon(
                        if (isBanned) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = if (isBanned) "Unban" else "Ban",
                        tint = if (isBanned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}
