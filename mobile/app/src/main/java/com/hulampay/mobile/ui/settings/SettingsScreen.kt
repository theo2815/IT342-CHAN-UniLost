package com.hulampay.mobile.ui.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    var selectedTheme by remember { mutableStateOf("light") }

    // Store originals for cancel
    var origFirstName by remember { mutableStateOf(firstName) }
    var origLastName by remember { mutableStateOf(lastName) }
    var origPhone by remember { mutableStateOf(phone) }
    var origAddress by remember { mutableStateOf(address) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Edit Profile",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Slate600,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}",
                                    color = White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        if (isEditing) {
                            TextButton(onClick = { /* Mock: change photo */ }) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Change Photo")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Editable fields
                    Text("Account Details", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Slate400)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            enabled = isEditing,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = settingsFieldColors()
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            enabled = isEditing,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = settingsFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        enabled = isEditing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = settingsFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        enabled = isEditing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = settingsFieldColors()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Read-only fields
                    Text("Private Details", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Slate400)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email (Read-only)") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = settingsFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = studentId,
                        onValueChange = {},
                        label = { Text("Student ID (Read-only)") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = settingsFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = school,
                        onValueChange = {},
                        label = { Text("School (Read-only)") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = settingsFieldColors()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isEditing) {
                            Button(
                                onClick = {
                                    origFirstName = firstName
                                    origLastName = lastName
                                    origPhone = phone
                                    origAddress = address
                                    isEditing = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate600)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    firstName = origFirstName
                                    lastName = origLastName
                                    phone = origPhone
                                    address = origAddress
                                    isEditing = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    // Mock save
                                    origFirstName = firstName
                                    origLastName = lastName
                                    origPhone = phone
                                    origAddress = address
                                    isEditing = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate600)
                            ) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }

            // Theme Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Theme Preferences",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Choose how UniLost looks to you.",
                        fontSize = 14.sp,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ThemeOptionCard(
                            label = "Light Mode",
                            isSelected = selectedTheme == "light",
                            onClick = { selectedTheme = "light" },
                            previewColors = listOf(Color(0xFFF1F5F9), Color(0xFFFFFFFF), Color(0xFF475569)),
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            label = "Dark Mode",
                            isSelected = selectedTheme == "dark",
                            onClick = { selectedTheme = "dark" },
                            previewColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF94A3B8)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Password Section (placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Change Password",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Slate800
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Coming soon - Password change functionality will be available when backend APIs are connected.",
                        fontSize = 13.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notification Preferences Section (placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Notification Preferences",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Slate800
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Coming soon - Customize which notifications you receive.",
                        fontSize = 13.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    previewColors: List<Color>, // [background, header, text]
    modifier: Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, Slate600) else BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Slate600.copy(alpha = 0.05f) else White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mini preview
            Card(
                shape = RoundedCornerShape(8.dp),
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
                    Column(modifier = Modifier.padding(8.dp)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp),
                            shape = RoundedCornerShape(3.dp),
                            color = previewColors[2].copy(alpha = 0.3f)
                        ) {}
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(6.dp),
                            shape = RoundedCornerShape(3.dp),
                            color = previewColors[2].copy(alpha = 0.2f)
                        ) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Slate800
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = Slate600,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun settingsFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = Slate600,
    unfocusedBorderColor = Color.LightGray,
    cursorColor = Slate600,
    containerColor = White,
    disabledBorderColor = Color.LightGray.copy(alpha = 0.5f),
    disabledTextColor = Slate400,
    disabledLabelColor = Slate400
)
