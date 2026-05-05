package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
    var dateText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isFormValid = title.isNotBlank() && description.isNotBlank() && category.isNotBlank() && location.isNotBlank()

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Report an Item",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(UniLostSpacing.md),
            verticalArrangement = Arrangement.spacedBy(UniLostSpacing.md)
        ) {
            // Type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
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
                    color = Sage400,
                    modifier = Modifier.weight(1f),
                    onClick = { type = "FOUND" }
                )
            }

            // Title
            UniLostTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title *",
                placeholder = "e.g. Black Samsung Galaxy S24"
            )

            // Description
            UniLostTextField(
                value = description,
                onValueChange = { if (it.length <= 1000) description = it },
                label = "Description *",
                placeholder = "Describe the item in detail...",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                supportingText = "${description.length}/1000"
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                UniLostTextField(
                    value = category,
                    onValueChange = {},
                    label = "Category *",
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor()
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
            UniLostTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location *",
                placeholder = "e.g. CIT-U Main Library, 2nd Floor",
                leadingIcon = Icons.Default.LocationOn
            )

            // Date picker
            UniLostTextField(
                value = dateText,
                onValueChange = {},
                label = if (type == "LOST") "Date Lost" else "Date Found",
                placeholder = "Select a date",
                readOnly = true,
                leadingIcon = Icons.Default.CalendarToday,
                modifier = Modifier.clickable { showDatePicker = true }
            )

            // Image upload placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.outline,
                        UniLostShapes.md
                    )
                    .clickable { /* image picker */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Tap to add photos (max 3)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Secret detail (FOUND only)
            if (type == "FOUND") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = UniLostShapes.md,
                    colors = CardDefaults.cardColors(
                        containerColor = Sage400_8pct
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(Sage400.copy(alpha = 0.3f))
                    )
                ) {
                    Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Sage400
                            )
                            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                            Text(
                                "Secret Detail",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        UniLostTextField(
                            value = secretDetail,
                            onValueChange = { secretDetail = it },
                            placeholder = "e.g. There is a sticker on the back"
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                        Text(
                            "Enter a detail only the true owner would know. This helps verify claims.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            // Submit button
            UniLostButton(
                text = "Post Item",
                onClick = {
                    Toast.makeText(context, "Item posted successfully!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                enabled = isFormValid
            )

            Spacer(modifier = Modifier.height(UniLostSpacing.md))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            dateText = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
            shape = UniLostShapes.lg
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
fun TypeOption(label: String, isSelected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = UniLostShapes.md,
        color = if (isSelected) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            width = 2.dp,
            brush = SolidColor(if (isSelected) color else MaterialTheme.colorScheme.outline)
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
