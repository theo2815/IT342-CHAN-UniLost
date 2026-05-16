package com.hulampay.mobile.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.AlertType
import com.hulampay.mobile.ui.components.AuthLogoHeader
import com.hulampay.mobile.ui.components.UniLostAlert
import com.hulampay.mobile.ui.components.UniLostButton
import com.hulampay.mobile.ui.components.UniLostTextField
import com.hulampay.mobile.ui.theme.Success
import com.hulampay.mobile.ui.theme.Warning
import com.hulampay.mobile.utils.*

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    email: String,
    resetToken: String,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var apiError        by remember { mutableStateOf<String?>(null) }

    val resetState by viewModel.resetState.collectAsState()
    val context    = LocalContext.current

    val passwordScore  = calculatePasswordStrength(newPassword)
    val strengthLevel  = getStrengthLevel(passwordScore)
    val passwordError  = when {
        newPassword.isNotEmpty() && !isPasswordValid(newPassword) ->
            "8+ chars with uppercase, number & special char (e.g. !@#\$)"
        else -> null
    }
    val confirmError = when {
        confirmPassword.isNotEmpty() && confirmPassword != newPassword -> "Passwords don't match"
        else -> null
    }

    LaunchedEffect(resetState) {
        when (resetState) {
            is UiState.Success -> {
                Toast.makeText(context, "Password reset! Please sign in.", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is UiState.Error -> {
                apiError = (resetState as UiState.Error).message
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Brand header ──────────────────────────────────────────────────
            AuthLogoHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Success-feel accent icon ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Set New Password",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = buildAnnotatedString {
                    append("Create a new password for ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(email)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Inline API error (mirrors website's <Alert type="error">)
            apiError?.let {
                UniLostAlert(
                    message = it,
                    type = AlertType.ERROR,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // ── Form card ─────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 0.dp,
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    UniLostTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            if (apiError != null) apiError = null
                        },
                        label = "New Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        errorMessage = passwordError,
                    )

                    if (newPassword.isNotEmpty()) {
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
                        onValueChange = {
                            confirmPassword = it
                            if (apiError != null) apiError = null
                        },
                        label = "Confirm New Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        errorMessage = confirmError,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val canSubmit = isPasswordValid(newPassword) &&
                        confirmPassword == newPassword &&
                        confirmPassword.isNotEmpty()

                    UniLostButton(
                        text = if (resetState is UiState.Loading) "Resetting..." else "Reset Password",
                        onClick = {
                            viewModel.resetPassword(
                                email           = email,
                                resetToken      = resetToken,
                                newPassword     = newPassword,
                                confirmPassword = confirmPassword,
                            )
                        },
                        isLoading = resetState is UiState.Loading,
                        enabled   = canSubmit,
                        trailingIcon = if (resetState !is UiState.Loading) {
                            Icons.AutoMirrored.Filled.ArrowForward
                        } else null,
                    )
                }
            }

            // ── Footer: Back to Sign In ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "Back to Sign In",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── Password strength bar (mirrors RegisterScreen) ────────────────────────────
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
