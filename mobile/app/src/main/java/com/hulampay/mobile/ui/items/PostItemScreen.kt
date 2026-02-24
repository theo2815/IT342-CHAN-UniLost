package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItemScreen(navController: NavController) {
    var type by remember { mutableStateOf("LOST") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var secretDetail by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isFormValid = title.isNotBlank() && description.isNotBlank() && category.isNotBlank() && location.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report an Item") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeOption(
                    label = "I Lost Something",
                    isSelected = type == "LOST",
                    color = ErrorRed,
                    modifier = Modifier.weight(1f),
                    onClick = { type = "LOST"; secretDetail = "" }
                )
                TypeOption(
                    label = "I Found Something",
                    isSelected = type == "FOUND",
                    color = Sage,
                    modifier = Modifier.weight(1f),
                    onClick = { type = "FOUND" }
                )
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                placeholder = { Text("e.g. Black Samsung Galaxy S24") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Slate600,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 1000) description = it },
                label = { Text("Description *") },
                placeholder = { Text("Describe the item in detail...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Slate600,
                    unfocusedBorderColor = Color.LightGray
                ),
                supportingText = { Text("${description.length}/1000", fontSize = 11.sp) }
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Slate600,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    MockItems.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { category = cat; categoryExpanded = false }
                        )
                    }
                }
            }

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location *") },
                placeholder = { Text("e.g. CIT-U Main Library, 2nd Floor") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Slate600,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

            // Image upload placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .clickable { /* image picker */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp), tint = Slate400)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap to add photos (max 3)", fontSize = 13.sp, color = Slate400)
                }
            }

            // Secret detail (FOUND only)
            if (type == "FOUND") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Sage.copy(alpha = 0.05f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Sage.copy(alpha = 0.3f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = Sage)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Secret Detail", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Slate800)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = secretDetail,
                            onValueChange = { secretDetail = it },
                            placeholder = { Text("e.g. There is a sticker on the back") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Sage,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Enter a detail only the true owner would know. This helps verify claims.",
                            fontSize = 11.sp,
                            color = Slate400,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit button
            Button(
                onClick = {
                    Toast.makeText(context, "Item posted successfully!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate600),
                enabled = isFormValid
            ) {
                Text("Post Item", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TypeOption(label: String, isSelected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.08f) else White,
        border = CardDefaults.outlinedCardBorder().copy(
            width = 2.dp,
            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) color else Color.LightGray)
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (isSelected) color else Slate600
            )
        }
    }
}
