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
import androidx.compose.ui.graphics.Color
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
    var schoolId by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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
                text = "HulamPay",
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
                text = "Join your campus marketplace today.",
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
                // School Dropdown
                // School Dropdown
                val schools by viewModel.schoolsState.collectAsState()
                var expanded by remember { mutableStateOf(false) }
                var selectedSchoolName by remember { mutableStateOf("") }

                LaunchedEffect(schools) {
                    if (schools.isNotEmpty()) {
                        selectedSchoolName = schools[0].name
                        schoolId = "1" // Default to first school or handle properly
                        // Ideally we should map the selected school's ID, but for now defaulting logic as placeholder
                        // better logic:
                         schoolId = schools[0].schoolId
                    }
                }

                Text("Select University", color = Slate600, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedSchoolName,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Slate400) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Slate600,
                            unfocusedBorderColor = Color.LightGray,
                            containerColor = White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(White)
                    ) {
                        schools.forEach { school ->
                            DropdownMenuItem(
                                text = { Text(school.name, color = Slate800) },
                                onClick = {
                                    selectedSchoolName = school.name
                                    schoolId = school.schoolId
                                    expanded = false
                                }
                            )
                        }
                    }
                }

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
                    label = "School Email",
                    icon = Icons.Default.Email,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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

                Text("Student Verification (Optional)", color = Slate400, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))

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
                    text = "Join HulamPay",
                    onClick = {
                        viewModel.register(
                            firstName, lastName, email, password, confirmPassword,
                            address, "09123456789", studentId, schoolId
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
