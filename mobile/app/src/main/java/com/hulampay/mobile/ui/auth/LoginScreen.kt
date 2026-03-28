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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.UniLostButton
import com.hulampay.mobile.ui.components.UniLostTextField
import com.hulampay.mobile.utils.UiState

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Success -> {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is UiState.Error -> {
                Toast.makeText(
                    context,
                    (loginState as UiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
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
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Brand header ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "UL",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "UniLost",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Campus Lost & Found Network",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Form card ─────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Sign in to your account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                    )

                    // Email field
                    UniLostTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "University Email",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    // Password field
                    UniLostTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // Remember me + Forgot password row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { rememberMe = !rememberMe },
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Remember me",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.ForgotPassword.route)
                            },
                        )
                    }

                    // Sign in button
                    UniLostButton(
                        text = "Sign In",
                        onClick = { viewModel.login(email, password) },
                        isLoading = loginState is UiState.Loading,
                    )

                    // Register link
                    Row(
                        modifier = Modifier.padding(top = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "New to UniLost? ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Register.route)
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
