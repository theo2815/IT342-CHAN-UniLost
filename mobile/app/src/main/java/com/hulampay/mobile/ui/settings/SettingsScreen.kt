package com.hulampay.mobile.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.PasswordStrengthLevel
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.calculatePasswordStrength
import com.hulampay.mobile.utils.getStrengthLevel
import com.hulampay.mobile.utils.isPasswordValid

private enum class SettingsTab(val label: String, val icon: ImageVector) {
    PROFILE("Edit Profile", Icons.Outlined.Person),
    PASSWORD("Change Password", Icons.Outlined.Lock),
    NOTIFICATIONS("Notifications", Icons.Outlined.Notifications),
    THEME("Theme", Icons.Outlined.Palette),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf(SettingsTab.PROFILE) }

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
        ) {
            SettingsTabRow(
                selected = selectedTab,
                onSelect = { selectedTab = it },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = UniLostSpacing.md)
            ) {
                SettingsTabHeader(tab = selectedTab)

                when (selectedTab) {
                    SettingsTab.PROFILE       -> EditProfileTab(viewModel = viewModel)
                    SettingsTab.PASSWORD      -> ChangePasswordTab(viewModel = viewModel)
                    SettingsTab.NOTIFICATIONS -> NotificationsTab(viewModel = viewModel)
                    SettingsTab.THEME         -> ThemeTab()
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.lg))
            }
        }
    }
}

@Composable
private fun SettingsTabRow(
    selected: SettingsTab,
    onSelect: (SettingsTab) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsTab.values().forEach { tab ->
                    SettingsTabChip(
                        tab = tab,
                        isSelected = tab == selected,
                        onClick = { onSelect(tab) },
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun SettingsTabChip(
    tab: SettingsTab,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val labelColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = UniLostSpacing.sm, vertical = UniLostSpacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.xs),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                tab.label,
                style = MaterialTheme.typography.labelLarge,
                color = labelColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(40.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(2.dp),
                )
        )
    }
}

@Composable
private fun SettingsTabHeader(tab: SettingsTab) {
    Column(modifier = Modifier.padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm)) {
        Text(
            tab.label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
        Text(
            "Manage your account settings and preferences.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
        shape = UniLostShapes.lg,
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(UniLostSpacing.lg), content = content)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    Spacer(modifier = Modifier.height(UniLostSpacing.md))
}

// ═══════════════════════════════════════════════════════════════
// EDIT PROFILE TAB
// ═══════════════════════════════════════════════════════════════
@Composable
private fun EditProfileTab(viewModel: SettingsViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val profileState by viewModel.profileState.collectAsState()
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }
    var fullNameField by remember(user?.id, isEditing) { mutableStateOf(user?.fullName.orEmpty()) }
    var pendingPhoto by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) pendingPhoto = uri
    }

    LaunchedEffect(profileState) {
        when (val s = profileState) {
            is UiState.Success -> {
                Toast.makeText(context, s.data, Toast.LENGTH_SHORT).show()
                isEditing = false
                pendingPhoto = null
                viewModel.consumeProfileState()
            }
            is UiState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.consumeProfileState()
            }
            else -> {}
        }
    }

    val isSaving = profileState is UiState.Loading
    val resolvedFullName = user?.fullName.orEmpty()
    val firstName = resolvedFullName.substringBefore(' ', resolvedFullName)
    val lastName  = resolvedFullName.substringAfter(' ', "")
    val schoolLabel = user?.campus?.name?.takeIf { it.isNotBlank() }
        ?: user?.universityTag?.takeIf { it.isNotBlank() }
        ?: "Unknown School"

    SectionCard {
        // Avatar row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AvatarView(
                    firstName = firstName,
                    lastName = lastName,
                    size = 80.dp,
                    imageUrl = pendingPhoto?.toString() ?: user?.profilePictureUrl,
                )
                if (isEditing) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(UniLostSpacing.md))
            if (isEditing) {
                TextButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                    Text("Change Photo", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    SectionCard {
        SectionTitle("Account Details")

        UniLostTextField(
            value = if (isEditing) fullNameField else resolvedFullName,
            onValueChange = { fullNameField = it },
            label = "Full Name",
            enabled = isEditing,
        )

        Spacer(modifier = Modifier.height(UniLostSpacing.sm))

        UniLostTextField(
            value = schoolLabel,
            onValueChange = {},
            label = "School (Read-only)",
            enabled = false,
            leadingIcon = Icons.Default.School,
        )
    }

    SectionCard {
        SectionTitle("Private Details")

        UniLostTextField(
            value = user?.email.orEmpty(),
            onValueChange = {},
            label = "Email Address (Read-only)",
            enabled = false,
            leadingIcon = Icons.Default.Email,
        )

        Spacer(modifier = Modifier.height(UniLostSpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
        ) {
            UniLostTextField(
                value = user?.role.orEmpty().ifBlank { "STUDENT" },
                onValueChange = {},
                label = "Role",
                enabled = false,
                modifier = Modifier.weight(1f),
            )
            UniLostTextField(
                value = (user?.karmaScore ?: 0).toString(),
                onValueChange = {},
                label = "Karma Score",
                enabled = false,
                modifier = Modifier.weight(1f),
            )
        }
    }

    // Action buttons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isEditing) {
            UniLostButton(
                text = "Edit Profile",
                onClick = {
                    fullNameField = resolvedFullName
                    pendingPhoto = null
                    isEditing = true
                },
                icon = Icons.Default.Edit,
                fillWidth = false,
            )
        } else {
            UniLostButton(
                text = "Cancel",
                onClick = {
                    fullNameField = resolvedFullName
                    pendingPhoto = null
                    isEditing = false
                },
                variant = ButtonVariant.DANGER,
                icon = Icons.Default.Close,
                fillWidth = false,
                enabled = !isSaving,
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            UniLostButton(
                text = if (isSaving) "Saving..." else "Save Changes",
                onClick = { viewModel.saveProfile(fullNameField, pendingPhoto) },
                icon = Icons.Default.Save,
                isLoading = isSaving,
                fillWidth = false,
                enabled = fullNameField.isNotBlank(),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CHANGE PASSWORD TAB
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ChangePasswordTab(viewModel: SettingsViewModel) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordState by viewModel.passwordState.collectAsState()
    val context = LocalContext.current

    val passwordScore = calculatePasswordStrength(newPassword)
    val strengthLevel = getStrengthLevel(passwordScore)
    val newPasswordError = when {
        newPassword.isNotEmpty() && !isPasswordValid(newPassword) ->
            "8+ chars with uppercase, number & special char (e.g. !@#\$)"
        else -> null
    }
    val confirmError = when {
        confirmPassword.isNotEmpty() && confirmPassword != newPassword -> "Passwords don't match"
        else -> null
    }

    LaunchedEffect(passwordState) {
        when (val s = passwordState) {
            is UiState.Success -> {
                Toast.makeText(context, s.data, Toast.LENGTH_SHORT).show()
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                viewModel.consumePasswordState()
            }
            is UiState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.consumePasswordState()
            }
            else -> {}
        }
    }

    SectionCard {
        SectionTitle("Change Your Password")
        Text(
            "For security, please enter your current password before setting a new one.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(UniLostSpacing.md))

        UniLostTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = "Current Password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
        )

        Spacer(modifier = Modifier.height(UniLostSpacing.sm))

        UniLostTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = "New Password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            errorMessage = newPasswordError,
        )

        if (newPassword.isNotEmpty()) {
            PasswordStrengthBar(
                score = passwordScore,
                level = strengthLevel,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
        } else {
            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
        }

        UniLostTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm New Password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            errorMessage = confirmError,
        )

        Spacer(modifier = Modifier.height(UniLostSpacing.md))

        val canSubmit = currentPassword.isNotBlank() &&
            isPasswordValid(newPassword) &&
            confirmPassword == newPassword &&
            confirmPassword.isNotEmpty()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            UniLostButton(
                text = if (passwordState is UiState.Loading) "Changing..." else "Change Password",
                onClick = {
                    viewModel.changePassword(
                        currentPassword = currentPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                    )
                },
                icon = Icons.Outlined.Lock,
                isLoading = passwordState is UiState.Loading,
                enabled = canSubmit,
                fillWidth = false,
            )
        }
    }
}

@Composable
private fun PasswordStrengthBar(
    score: Int,
    level: PasswordStrengthLevel,
    modifier: Modifier = Modifier,
) {
    val color = when (level) {
        PasswordStrengthLevel.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrengthLevel.MEDIUM -> Warning
        PasswordStrengthLevel.STRONG -> Success
    }
    val fraction = score.coerceIn(0, 5) / 5f

    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = fraction,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = "Strength: ${level.label}",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// NOTIFICATIONS TAB
// ═══════════════════════════════════════════════════════════════
@Composable
private fun NotificationsTab(viewModel: SettingsViewModel) {
    val enabled by viewModel.notificationsEnabled.collectAsState()
    val context = LocalContext.current

    SectionCard {
        SectionTitle("Notification Preferences")
        Text(
            "Control how you receive notifications in the app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(UniLostSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "In-App Notifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    if (enabled) {
                        "You will see badge counts and popup alerts for new notifications."
                    } else {
                        "Notification badges and popup alerts are hidden. You can still view notifications manually."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(UniLostSpacing.md))
            Switch(
                checked = enabled,
                onCheckedChange = { newValue ->
                    viewModel.setNotificationsEnabled(newValue)
                    Toast.makeText(
                        context,
                        if (newValue) "Notifications enabled" else "Notifications disabled",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// THEME TAB
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ThemeTab() {
    val themePreference = LocalThemePreference.current

    SectionCard {
        SectionTitle("Theme Preferences")
        Text(
            "Choose how UniLost looks to you. Select a single theme that will apply across all pages.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(UniLostSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.md),
        ) {
            ThemeOptionCard(
                label = "Light Mode",
                isSelected = themePreference.value == ThemePreference.LIGHT,
                onClick = { themePreference.value = ThemePreference.LIGHT },
                background = Slate100,
                headerColor = White,
                lineColor = Slate600,
                modifier = Modifier.weight(1f),
            )
            ThemeOptionCard(
                label = "Dark Mode",
                isSelected = themePreference.value == ThemePreference.DARK,
                onClick = { themePreference.value = ThemePreference.DARK },
                background = Slate900,
                headerColor = Slate800,
                lineColor = Slate400,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    background: Color,
    headerColor: Color,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = UniLostShapes.lg,
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
            },
        ),
    ) {
        Box {
            Column(
                modifier = Modifier.padding(UniLostSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Card(
                    shape = UniLostShapes.md,
                    colors = CardDefaults.cardColors(containerColor = background),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                ) {
                    Column {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp),
                            color = headerColor.copy(alpha = 0.7f),
                        ) {}
                        Column(modifier = Modifier.padding(UniLostSpacing.sm)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = lineColor.copy(alpha = 0.3f),
                            ) {}
                            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(8.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = lineColor.copy(alpha = 0.3f),
                            ) {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.md))

                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(UniLostSpacing.sm)
                        .size(24.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}
