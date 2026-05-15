package com.hulampay.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hulampay.mobile.ui.theme.*

/**
 * UniLost Top App Bar matching spec Section 8.6.
 *
 * Three variants:
 * 1. Main (authenticated) — Logo + "UniLost" + notification bell + chat icon
 * 2. Main (guest) — Logo + "UniLost" + Login + Register buttons
 * 3. Detail — Back arrow + title + optional action icons
 */

/**
 * Main top bar for authenticated users.
 * Shows "UniLost" title with notification and chat action icons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniLostTopBar(
    onLogoClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    notificationCount: Int = 0,
    chatCount: Int = 0,
    chatActive: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = "UniLost",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLogoClick,
                )
            )
        },
        actions = {
            // Notifications
            IconButton(onClick = onNotificationsClick) {
                if (notificationCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text("$notificationCount")
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Chat — when the user is on the Messages page (chatActive == true),
            // render the IconButton with a primary-tinted circular container so it
            // mirrors the active-tab pill treatment used in BottomNavBar.
            val chatIconTint = if (chatActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val chatButtonColors = if (chatActive) {
                IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButtonDefaults.iconButtonColors()
            }
            IconButton(onClick = onChatClick, colors = chatButtonColors) {
                if (chatCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text("$chatCount")
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Messages",
                            tint = chatIconTint
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = "Messages",
                        tint = chatIconTint
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Main top bar for guest users.
 * Shows "UniLost" title with Login and Register action buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniLostGuestTopBar(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = "UniLost",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            TextButton(onClick = onLoginClick) {
                Text(
                    "Login",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            OutlinedButton(
                onClick = onRegisterClick,
                shape = UniLostShapes.sm,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    "Register",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Logo-only top bar for primary tab screens that don't expose the chat /
 * notification action icons (e.g. Profile). Shows the "UniLost" logo as the
 * title and accepts arbitrary action slots on the right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniLostLogoTopBar(
    onLogoClick: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = "UniLost",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLogoClick,
                )
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Detail-screen top bar with back navigation.
 * Shows back arrow + title + optional action icons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniLostDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
    )
}
