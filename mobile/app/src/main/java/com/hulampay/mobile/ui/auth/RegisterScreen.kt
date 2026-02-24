package com.hulampay.mobile.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.AuthInput
import com.hulampay.mobile.ui.components.PrimaryButton
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
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
            .background(Slate100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "UniLost",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Create Account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Join the Cebu City campus lost & found network.",
                fontSize = 14.sp,
                color = Slate400,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Form Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, shape = RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    AuthInput(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First Name",
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    AuthInput(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last Name",
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }

                AuthInput(
                    value = email,
                    onValueChange = { email = it },
                    label = "University Email (e.g., name@cit.edu)",
                    icon = Icons.Default.Email,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // School detection feedback
                if (detectedSchool != null) {
                    Text(
                        text = "Detected: ${detectedSchool.name} (${detectedSchool.shortName})",
                        fontSize = 12.sp,
                        color = Sage,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                } else if (email.contains("@")) {
                    Text(
                        text = "Supported: cit.edu, usc.edu.ph, usjr.edu.ph, uc.edu.ph, up.edu.ph, swu.edu.ph, cnu.edu.ph, ctu.edu.ph",
                        fontSize = 11.sp,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                AuthInput(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AuthInput(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AuthInput(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    icon = Icons.Default.CheckCircle,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("Additional Info (Optional)", color = Slate400, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                     AuthInput(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = "Student ID",
                        icon = Icons.Default.AccountBox,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                }

                PrimaryButton(
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
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Already a member? ", color = Slate600, fontSize = 14.sp)
                    Text(
                        text = "Sign In",
                        color = Sage,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { navController.navigate(Screen.Login.route) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
