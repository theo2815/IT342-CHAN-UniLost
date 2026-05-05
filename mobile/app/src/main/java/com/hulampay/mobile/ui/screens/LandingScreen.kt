package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

/**
 * Guest Landing / Home screen — Spec Section 10.1.
 * Layout-only stub with no API calls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(navController: NavController) {
    Scaffold(
        topBar = {
            UniLostGuestTopBar(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md)
                    .padding(top = UniLostSpacing.md)
                    .background(
                        Brush.linearGradient(listOf(Slate700, Slate800)),
                        shape = UniLostShapes.xl
                    )
                    .padding(UniLostSpacing.xl)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Lost Something\non Campus?",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    Text(
                        text = "Connect with your campus community to find lost items faster.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Slate300,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.lg))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                    ) {
                        UniLostButton(
                            text = "Report Lost",
                            onClick = { navController.navigate(Screen.Login.route) },
                            variant = ButtonVariant.SECONDARY,
                            icon = Icons.Default.SearchOff,
                            fillWidth = false
                        )
                        UniLostButton(
                            text = "I Found Something",
                            onClick = { navController.navigate(Screen.Login.route) },
                            icon = Icons.Default.Inventory2,
                            fillWidth = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.xl))

            // How It Works Section
            Text(
                text = "How It Works",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = UniLostSpacing.md),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            Column(
                modifier = Modifier.padding(horizontal = UniLostSpacing.md),
                verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
            ) {
                HowItWorksStep(
                    step = "1",
                    icon = Icons.Default.Edit,
                    title = "Report Your Item",
                    description = "Post details about your lost or found item with photos and location."
                )
                HowItWorksStep(
                    step = "2",
                    icon = Icons.Default.People,
                    title = "Community Helps",
                    description = "Students across campus see your post and can submit claims."
                )
                HowItWorksStep(
                    step = "3",
                    icon = Icons.Default.Handshake,
                    title = "Safe Handover",
                    description = "Verify ownership and arrange a secure meetup on campus."
                )
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.xl))

            // CTA Footer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                shape = UniLostShapes.lg,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UniLostSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Ready to get started?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    Text(
                        "Join the Cebu City campus lost & found network.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))
                    UniLostButton(
                        text = "Create Account",
                        onClick = { navController.navigate(Screen.Register.route) },
                        fillWidth = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.xxl))
        }
    }
}

@Composable
private fun HowItWorksStep(
    step: String,
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(UniLostSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = UniLostShapes.md,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(UniLostSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Step $step: $title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
