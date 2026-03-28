package com.hulampay.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.User
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.BottomNavBar
import com.hulampay.mobile.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val uiState     by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "UniLost",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            when {
                // Loading with no cached user → show spinner
                currentUser == null && uiState is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                // No user and error → show retry
                currentUser == null && uiState is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = (uiState as UiState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.loadCurrentUser() }) {
                            Text("Retry")
                        }
                    }
                }
                // Have user data → show dashboard
                else -> {
                    DashboardContent(
                        user        = currentUser,
                        navController = navController,
                    )
                }
            }
        }
    }
}

// ── Dashboard content ─────────────────────────────────────────────────────────
@Composable
private fun DashboardContent(
    user: User?,
    navController: NavController,
) {
    // ── Greeting banner ───────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Column {
            Text(
                text = if (user != null) "Hi, ${user.firstName}! 👋" else "Welcome!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (user != null) "${user.karmaScore} karma points" else "Loading profile...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f),
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ── Section label ─────────────────────────────────────────────────────
    Text(
        text = "QUICK ACTIONS",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 24.dp),
    )

    Spacer(modifier = Modifier.height(12.dp))

    // ── Quick action cards ────────────────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        QuickActionCard(
            title    = "Report Lost Item",
            subtitle = "I lost something",
            icon     = Icons.Default.Search,
            onClick  = { navController.navigate(Screen.PostItem.route) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )
        QuickActionCard(
            title    = "Found Something",
            subtitle = "I found an item",
            icon     = Icons.Default.AddCircle,
            onClick  = { navController.navigate(Screen.PostItem.route) },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ── Browse items button ───────────────────────────────────────────────
    Button(
        onClick = { navController.navigate(Screen.ItemFeed.route) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor   = MaterialTheme.colorScheme.onSecondary,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.List,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Browse Lost & Found Items",
            style = MaterialTheme.typography.labelLarge,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── My items shortcut ─────────────────────────────────────────────────
    OutlinedButton(
        onClick = { navController.navigate(Screen.MyItems.route) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "My Posted Items",
            style = MaterialTheme.typography.labelLarge,
        )
    }

    Spacer(modifier = Modifier.height(32.dp))
}

// ── Quick action card ─────────────────────────────────────────────────────────
@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
