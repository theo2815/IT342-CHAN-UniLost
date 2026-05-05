package com.hulampay.mobile.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.UniLostButton
import com.hulampay.mobile.ui.components.UniLostTextField
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.calculatePasswordStrength
import com.hulampay.mobile.utils.getStrengthLevel
import com.hulampay.mobile.utils.isPasswordValid
import com.hulampay.mobile.utils.PasswordStrengthLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var fullName        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedCampusId by remember { mutableStateOf<String?>(null) }
    var agreedToTerms   by remember { mutableStateOf(false) }
    var campusDropdownExpanded by remember { mutableStateOf(false) }

    val registerState   by viewModel.registerState.collectAsState()
    val matchedCampuses by viewModel.matchedCampuses.collectAsState()
    val context         = LocalContext.current
    val scrollState     = rememberScrollState()

    // Inline validation states
    val passwordScore   = calculatePasswordStrength(password)
    val strengthLevel   = getStrengthLevel(passwordScore)
    val passwordError   = when {
        password.isNotEmpty() && !isPasswordValid(password) ->
            "8+ chars with uppercase, number & special char (e.g. !@#\$)"
        else -> null
    }
    val confirmError = when {
        confirmPassword.isNotEmpty() && confirmPassword != password -> "Passwords don't match"
        else -> null
    }

    // Auto-detect campus from email
    LaunchedEffect(email) { viewModel.onEmailChanged(email) }

    // Single campus auto-selected
    val autoCampus = matchedCampuses.takeIf { it.size == 1 }?.first()
    val effectiveCampusId = autoCampus?.id ?: selectedCampusId

    LaunchedEffect(registerState) {
        when (registerState) {
            is UiState.Success -> {
                Toast.makeText(context, "Account created! Please sign in.", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            }
            is UiState.Error -> {
                Toast.makeText(context, (registerState as UiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── Brand header ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "UL",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Join the campus lost & found community",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Form card ─────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 0.dp,
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // ── Personal Info ─────────────────────────────────────────
                    FormSectionHeader("Personal Information")
                    Spacer(modifier = Modifier.height(12.dp))

                    UniLostTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // ── Account Details ───────────────────────────────────────
                    FormSectionHeader("Account Details")
                    Spacer(modifier = Modifier.height(12.dp))

                    UniLostTextField(
                        value = email,
                        onValueChange = { email = it; selectedCampusId = null },
                        label = "University Email",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                    )

                    // Campus detection feedback
                    Spacer(modifier = Modifier.height(6.dp))
                    when {
                        autoCampus != null -> {
                            // Single match — show green chip
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = autoCampus.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        matchedCampuses.size > 1 -> {
                            // Multiple matches — show dropdown
                            val selectedLabel = matchedCampuses
                                .find { it.id == selectedCampusId }?.displayName ?: "Select your campus"
                            ExposedDropdownMenuBox(
                                expanded = campusDropdownExpanded,
                                onExpandedChange = { campusDropdownExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = selectedLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Campus") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = campusDropdownExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                )
                                ExposedDropdownMenu(
                                    expanded = campusDropdownExpanded,
                                    onDismissRequest = { campusDropdownExpanded = false },
                                ) {
                                    matchedCampuses.forEach { campus ->
                                        DropdownMenuItem(
                                            text = { Text(campus.displayName) },
                                            onClick = {
                                                selectedCampusId = campus.id
                                                campusDropdownExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        email.contains("@") && matchedCampuses.isEmpty() -> {
                            Text(
                                text = "Domain not recognized. Supported: cit.edu · usc.edu.ph · usjr.edu.ph · uc.edu.ph · up.edu.ph · swu.edu.ph · cnu.edu.ph · ctu.edu.ph",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        }
                        else -> Spacer(modifier = Modifier.height(2.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password with strength indicator
                    UniLostTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        errorMessage = passwordError,
                    )
                    if (password.isNotEmpty()) {
                        PasswordStrengthBar(
                            score = passwordScore,
                            level = strengthLevel,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    UniLostTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        errorMessage = confirmError,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Terms of Service checkbox ─────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { agreedToTerms = !agreedToTerms },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I agree to the Terms of Service and Privacy Policy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Submit ────────────────────────────────────────────────
                    val canSubmit = fullName.isNotBlank() &&
                        email.isNotBlank() &&
                        matchedCampuses.isNotEmpty() &&
                        (matchedCampuses.size == 1 || !effectiveCampusId.isNullOrBlank()) &&
                        isPasswordValid(password) &&
                        confirmError == null &&
                        agreedToTerms

                    UniLostButton(
                        text = "Create Account",
                        onClick = {
                            viewModel.register(
                                fullName         = fullName,
                                email            = email,
                                password         = password,
                                confirmPassword  = confirmPassword,
                                campusId         = effectiveCampusId,
                                agreedToTerms    = agreedToTerms,
                            )
                        },
                        isLoading = registerState is UiState.Loading,
                        enabled   = canSubmit,
                    )

                    // Sign in link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Already have an account? ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { navController.navigate(Screen.Login.route) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── Password strength bar ─────────────────────────────────────────────────────
@Composable
private fun PasswordStrengthBar(
    score: Int,
    level: PasswordStrengthLevel,
    modifier: Modifier = Modifier,
) {
    val color = when (level) {
        PasswordStrengthLevel.WEAK   -> MaterialTheme.colorScheme.error
        PasswordStrengthLevel.MEDIUM -> Warning
        PasswordStrengthLevel.STRONG -> Success
    }
    val fraction = (score.coerceIn(0, 5) / 5f)

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

// ── Section header ────────────────────────────────────────────────────────────
@Composable
private fun FormSectionHeader(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
    Divider(
        modifier = Modifier.padding(top = 6.dp),
        color = MaterialTheme.colorScheme.outline,
    )
}
