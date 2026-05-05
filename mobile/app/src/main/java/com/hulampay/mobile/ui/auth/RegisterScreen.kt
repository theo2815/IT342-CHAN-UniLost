package com.hulampay.mobile.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.UniLostButton
import com.hulampay.mobile.ui.components.UniLostTextField
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val registerState by viewModel.registerState.collectAsState()
    val schools by viewModel.schoolsState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Auto-detect school from email domain
    val detectedSchool = remember(email, schools) {
        if (email.contains("@")) {
            val domain = email.substringAfter("@").lowercase()
            schools.find { it.emailDomain.lowercase() == domain }
        } else null
    }

    LaunchedEffect(registerState) {
        when (registerState) {
            is UiState.Success -> {
                Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(UniLostSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "UniLost",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = UniLostSpacing.md)
            )
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = UniLostSpacing.xs)
            )
            Text(
                text = "Join the Cebu City campus lost & found network.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = UniLostSpacing.lg)
            )

            // Form Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = UniLostShapes.xl)
                    .padding(UniLostSpacing.lg)
            ) {
                // Name row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                ) {
                    UniLostTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First Name",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                    UniLostTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last Name",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.md))

                // Email
                UniLostTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "University Email (e.g., name@cit.edu)",
                    leadingIcon = Icons.Default.Email
                )

                // School detection feedback
                if (detectedSchool != null) {
                    Text(
                        text = "Detected: ${detectedSchool.name} (${detectedSchool.shortName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            top = UniLostSpacing.xs,
                            bottom = UniLostSpacing.sm,
                            start = UniLostSpacing.xs
                        )
                    )
                } else if (email.contains("@")) {
                    Text(
                        text = "Supported: cit.edu, usc.edu.ph, usjr.edu.ph, uc.edu.ph, up.edu.ph, swu.edu.ph, cnu.edu.ph, ctu.edu.ph",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            top = UniLostSpacing.xs,
                            bottom = UniLostSpacing.sm,
                            start = UniLostSpacing.xs
                        )
                    )
                } else {
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))
                }

                // Address
                UniLostTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    leadingIcon = Icons.Default.LocationOn,
                )

                Spacer(modifier = Modifier.height(UniLostSpacing.md))

                // Password
                UniLostTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
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
                    }
                )

                Spacer(modifier = Modifier.height(UniLostSpacing.md))

                // Confirm Password
                UniLostTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    leadingIcon = Icons.Default.CheckCircle,
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(UniLostSpacing.md))

                Text(
                    "Additional Info (Optional)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = UniLostSpacing.sm)
                )

                UniLostTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = "Student ID",
                    leadingIcon = Icons.Default.AccountBox,
                )

                Spacer(modifier = Modifier.height(UniLostSpacing.lg))

                // Submit Button
                UniLostButton(
                    text = "Join UniLost",
                    onClick = {
                        viewModel.register(
                            firstName, lastName, email, password, confirmPassword,
                            address, "", studentId
                        )
                    },
                    isLoading = registerState is UiState.Loading
                )

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = UniLostSpacing.lg),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already a member? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(UniLostSpacing.xl))
        }
    }
}
