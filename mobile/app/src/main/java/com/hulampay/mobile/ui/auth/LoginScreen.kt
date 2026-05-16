package com.hulampay.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.AlertType
import com.hulampay.mobile.ui.components.AuthLogoHeader
import com.hulampay.mobile.ui.components.ButtonVariant
import com.hulampay.mobile.ui.components.UniLostAlert
import com.hulampay.mobile.ui.components.UniLostButton
import com.hulampay.mobile.ui.components.UniLostTextField
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var apiError by remember { mutableStateOf<String?>(null) }
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Success -> {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is UiState.Error -> {
                apiError = (loginState as UiState.Error).message
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(UniLostSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = UniLostShapes.xl
                )
                .padding(UniLostSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            AuthLogoHeader()

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Your campus lost & found network.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = UniLostSpacing.xl)
            )

            // Inline API error (mirrors website's <Alert type="error">)
            apiError?.let {
                UniLostAlert(
                    message = it,
                    type = AlertType.ERROR,
                    modifier = Modifier.padding(bottom = UniLostSpacing.md)
                )
            }

            // Email Input
            UniLostTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (apiError != null) apiError = null
                },
                label = "University Email",
                placeholder = "student@cit.edu",
                leadingIcon = Icons.Default.Email,
                modifier = Modifier.padding(bottom = UniLostSpacing.md)
            )

            // Password Input
            UniLostTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (apiError != null) apiError = null
                },
                label = "Password",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.padding(bottom = UniLostSpacing.lg)
            )

            // Options Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = UniLostSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { rememberMe = !rememberMe },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            // Sign In Button
            UniLostButton(
                text = if (loginState is UiState.Loading) "Signing in..." else "Sign In",
                onClick = { viewModel.login(email, password) },
                isLoading = loginState is UiState.Loading,
                trailingIcon = if (loginState !is UiState.Loading) {
                    Icons.AutoMirrored.Filled.ArrowForward
                } else null
            )

            // Footer
            Row(
                modifier = Modifier.padding(top = UniLostSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New to UniLost? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            UniLostButton(
                text = "Back to landing page",
                onClick = { navController.navigate(Screen.Landing.route) },
                variant = ButtonVariant.GHOST,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                isCompact = true,
                modifier = Modifier.padding(top = UniLostSpacing.sm)
            )
        }
    }
}
