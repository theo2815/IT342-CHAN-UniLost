package com.hulampay.mobile.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    // Mock user data
    var firstName by remember { mutableStateOf("Theo") }
    var lastName by remember { mutableStateOf("Chan") }
    var phone by remember { mutableStateOf("+63 912 345 6789") }
    var address by remember { mutableStateOf("Cebu City, Philippines") }
    val email = "theo.chan@cit.edu"
    val studentId = "2023-12345"
    val school = "Cebu Institute of Technology - University"

    var isEditing by remember { mutableStateOf(false) }
    val themePreference = LocalThemePreference.current

    // Store originals for cancel
    var origFirstName by remember { mutableStateOf(firstName) }
    var origLastName by remember { mutableStateOf(lastName) }
    var origPhone by remember { mutableStateOf(phone) }
    var origAddress by remember { mutableStateOf(address) }

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Settings",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Edit Profile Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UniLostSpacing.md),
                shape = UniLostShapes.lg,
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.lg)) {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    // Avatar row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AvatarView(
                            firstName = firstName,
                            lastName = lastName,
                            size = 64.dp
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.md))
                        if (isEditing) {
                            TextButton(onClick = { /* Mock: change photo */ }) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                                Text(
                                    "Change Photo",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(UniLostSpacing.lg))

                    // Editable fields
                    Text(
                        "Account Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                    ) {
                        UniLostTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = "First Name",
                            enabled = isEditing,
                            modifier = Modifier.weight(1f)
                        )
                        UniLostTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = "Last Name",
                            enabled = isEditing,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    UniLostTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        enabled = isEditing,
                        leadingIcon = Icons.Default.Phone
                    )

                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    UniLostTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address",
                        enabled = isEditing,
                        leadingIcon = Icons.Default.LocationOn
                    )

                    Spacer(modifier = Modifier.height(UniLostSpacing.lg))

                    // Read-only fields
                    Text(
                        "Private Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    UniLostTextField(
                        value = email,
                        onValueChange = {},
                        label = "Email (Read-only)",
                        enabled = false,
                        leadingIcon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    UniLostTextField(
                        value = studentId,
                        onValueChange = {},
                        label = "Student ID (Read-only)",
                        enabled = false,
                        leadingIcon = Icons.Default.Badge
                    )

                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    UniLostTextField(
                        value = school,
                        onValueChange = {},
                        label = "School (Read-only)",
                        enabled = false,
                        leadingIcon = Icons.Default.School
                    )

                    Spacer(modifier = Modifier.height(UniLostSpacing.lg))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isEditing) {
                            UniLostButton(
                                text = "Edit Profile",
                                onClick = {
                                    origFirstName = firstName
                                    origLastName = lastName
                                    origPhone = phone
                                    origAddress = address
                                    isEditing = true
                                },
                                icon = Icons.Default.Edit,
                                fillWidth = false
                            )
                        } else {
                            UniLostButton(
                                text = "Cancel",
                                onClick = {
                                    firstName = origFirstName
                                    lastName = origLastName
                                    phone = origPhone
                                    address = origAddress
                                    isEditing = false
                                },
                                variant = ButtonVariant.DANGER,
                                icon = Icons.Default.Close,
                                fillWidth = false
                            )
                            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                            UniLostButton(
                                text = "Save Changes",
                                onClick = {
                                    // Mock save
                                    origFirstName = firstName
                                    origLastName = lastName
                                    origPhone = phone
                                    origAddress = address
                                    isEditing = false
                                },
                                icon = Icons.Default.Save,
                                fillWidth = false
                            )
                        }
                    }
                }
            }

            // Theme Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                shape = UniLostShapes.lg,
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.lg)) {
                    Text(
                        "Theme Preferences",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Choose how UniLost looks to you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                    ) {
                        ThemeOptionCard(
                            label = "System",
                            isSelected = themePreference.value == ThemePreference.SYSTEM,
                            onClick = { themePreference.value = ThemePreference.SYSTEM },
                            previewColors = listOf(Slate200, Slate400, Slate600),
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            label = "Light",
                            isSelected = themePreference.value == ThemePreference.LIGHT,
                            onClick = { themePreference.value = ThemePreference.LIGHT },
                            previewColors = listOf(Slate100, White, Slate600),
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            label = "Dark",
                            isSelected = themePreference.value == ThemePreference.DARK,
                            onClick = { themePreference.value = ThemePreference.DARK },
                            previewColors = listOf(Slate900, Slate800, Slate400),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.md))

            // Change Password Section (placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                shape = UniLostShapes.lg,
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                        Text(
                            "Change Password",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    Text(
                        "Coming soon - Password change functionality will be available when backend APIs are connected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            // Notification Preferences Section (placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md),
                shape = UniLostShapes.lg,
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(UniLostSpacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                        Text(
                            "Notification Preferences",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    Text(
                        "Coming soon - Customize which notifications you receive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.lg))
        }
    }
}

@Composable
private fun ThemeOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    previewColors: List<androidx.compose.ui.graphics.Color>, // [background, header, text]
    modifier: Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = UniLostShapes.md,
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(UniLostSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mini preview
            Card(
                shape = UniLostShapes.sm,
                colors = CardDefaults.cardColors(containerColor = previewColors[0]),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Column {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        color = previewColors[1]
                    ) {}
                    Column(modifier = Modifier.padding(UniLostSpacing.sm)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp),
                            shape = UniLostShapes.xs,
                            color = previewColors[2].copy(alpha = 0.3f)
                        ) {}
                        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(6.dp),
                            shape = UniLostShapes.xs,
                            color = previewColors[2].copy(alpha = 0.2f)
                        ) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
