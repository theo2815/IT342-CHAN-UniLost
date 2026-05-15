package com.hulampay.mobile.ui.items

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.ItemCategory
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val MAX_IMAGES = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItemScreen(
    navController: NavController,
    itemId: String? = null,
    viewModel: PostItemViewModel = hiltViewModel(),
) {
    val isEditMode = !itemId.isNullOrBlank()

    var type by remember { mutableStateOf("LOST") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var secretDetail by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImageCount by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val submitState by viewModel.submitState.collectAsState()
    val prefill by viewModel.prefill.collectAsState()
    val isSubmitting = submitState is UiState.Loading

    // Trigger the prefill load once when the screen opens in edit mode.
    LaunchedEffect(itemId) {
        if (!itemId.isNullOrBlank()) viewModel.loadForEdit(itemId)
    }

    // Apply the loaded item into the form state exactly once.
    var prefillApplied by remember { mutableStateOf(false) }
    LaunchedEffect(prefill) {
        val source = prefill
        if (source != null && !prefillApplied) {
            type = source.type.ifBlank { "LOST" }
            title = source.title
            description = source.description
            category = ItemCategory.backendToDisplay(source.category).orEmpty()
            location = source.location.orEmpty()
            secretDetail = source.secretDetailQuestion.orEmpty()
            val millis = parseIsoLocalDateTime(source.dateLostFound)
            if (millis != null) {
                selectedDateMillis = millis
                dateText = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(millis))
            }
            existingImageCount = source.imageUrls.size
            prefillApplied = true
        }
    }

    val isFormValid = title.isNotBlank() && description.isNotBlank() &&
            category.isNotBlank() && location.isNotBlank() && !isSubmitting

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGES)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris.take(MAX_IMAGES)
        }
    }

    LaunchedEffect(submitState) {
        when (val s = submitState) {
            is UiState.Success -> {
                Toast.makeText(
                    context,
                    if (isEditMode) "Item updated successfully!" else "Item posted successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetSubmitState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.resetSubmitState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = if (isEditMode) "Edit Item" else "Report an Item",
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Category *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                UniLostSelectField(
                    selectedLabel = category,
                    placeholder = "Select a category",
                ) { close ->
                    ItemCategory.displayLabels.forEach { cat ->
                        UniLostDropdownItem(
                            text = cat,
                            active = category == cat,
                            onClick = {
                                category = cat
                                close()
                            },
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

            // Image upload
            ImagePickerArea(
                selectedUris = selectedImageUris,
                onPickClick = {
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemove = { uri ->
                    selectedImageUris = selectedImageUris.filterNot { it == uri }
                }
            )

            if (isEditMode && existingImageCount > 0 && selectedImageUris.isEmpty()) {
                Text(
                    "Keeping the original ${existingImageCount} image" +
                        if (existingImageCount == 1) "" else "s" +
                            ". Pick new ones above to replace them.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                text = when {
                    isSubmitting && isEditMode -> "Saving..."
                    isSubmitting -> "Posting..."
                    isEditMode -> "Save changes"
                    else -> "Post Item"
                },
                onClick = {
                    if (isEditMode && itemId != null) {
                        viewModel.update(
                            itemId = itemId,
                            type = type,
                            title = title,
                            description = description,
                            categoryDisplay = category,
                            location = location,
                            secretDetail = secretDetail,
                            dateMillis = selectedDateMillis,
                            imageUris = selectedImageUris,
                        )
                    } else {
                        viewModel.submit(
                            type = type,
                            title = title,
                            description = description,
                            categoryDisplay = category,
                            location = location,
                            secretDetail = secretDetail,
                            dateMillis = selectedDateMillis,
                            imageUris = selectedImageUris,
                        )
                    }
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
                            selectedDateMillis = millis
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
private fun ImagePickerArea(
    selectedUris: List<Uri>,
    onPickClick: () -> Unit,
    onRemove: (Uri) -> Unit,
) {
    if (selectedUris.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.outline,
                    UniLostShapes.md
                )
                .clickable(onClick = onPickClick),
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
                    "Tap to add photos (max $MAX_IMAGES)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
        ) {
            selectedUris.forEach { uri ->
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(UniLostShapes.md)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, UniLostShapes.md)
                ) {
                    val bitmap = rememberDownsampledBitmap(uri)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Remove badge
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clickable { onRemove(uri) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            if (selectedUris.size < MAX_IMAGES) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.outline,
                            UniLostShapes.md
                        )
                        .clip(UniLostShapes.md)
                        .clickable(onClick = onPickClick),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add more",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Add",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberDownsampledBitmap(uri: Uri, sampleSize: Int = 4): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    BitmapFactory.decodeStream(stream, null, opts)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    return bitmap
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

/**
 * Parse the backend's ISO-8601 LocalDateTime string ("2026-05-13T00:00:00") back
 * into millis-since-epoch. Returns null on blank input or format mismatch so the
 * caller can fall through to "no date picked".
 */
private fun parseIsoLocalDateTime(iso: String?): Long? {
    if (iso.isNullOrBlank()) return null
    // Backend serializes with optional fractional seconds (LocalDateTime with Jackson).
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm",
        "yyyy-MM-dd",
    )
    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getDefault()
            return sdf.parse(iso)?.time
        } catch (_: Exception) {
            // try the next pattern
        }
    }
    return null
}
