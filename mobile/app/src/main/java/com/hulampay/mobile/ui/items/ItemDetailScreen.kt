package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hulampay.mobile.data.model.ClaimDto
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    navController: NavController,
    itemId: String,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val incomingActionInFlight by viewModel.incomingActionInFlight.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.load(itemId)
    }

    when (val current = state) {
        UiState.Idle, UiState.Loading -> DetailLoadingScaffold(navController)
        is UiState.Error -> DetailErrorScaffold(navController, current.message)
        is UiState.Success -> ItemDetailContent(
            navController = navController,
            data = current.data,
            submitState = submitState,
            incomingActionInFlight = incomingActionInFlight,
            onSubmitClaim = viewModel::submitClaim,
            onResetSubmit = viewModel::resetSubmitState,
            onAcceptIncoming = viewModel::acceptIncomingClaim,
            onRejectIncoming = viewModel::rejectIncomingClaim,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailLoadingScaffold(navController: NavController) {
    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Item Details",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailErrorScaffold(navController: NavController, message: String) {
    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Not Found",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        EmptyState(
            icon = Icons.Default.SearchOff,
            title = "Item not found",
            message = message.ifBlank { "This item may have been removed or doesn't exist." },
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailContent(
    navController: NavController,
    data: ItemDetailData,
    submitState: SubmitClaimState,
    incomingActionInFlight: Boolean,
    onSubmitClaim: (message: String, providedAnswer: String?) -> Unit,
    onResetSubmit: () -> Unit,
    onAcceptIncoming: (claimId: String) -> Unit,
    onRejectIncoming: (claimId: String) -> Unit,
) {
    val item = data.item
    val context = LocalContext.current
    val isFound = item.type == "FOUND"
    val isPoster = data.isPoster
    val isAdmin = data.isAdmin

    val posterName = item.reporter?.fullName.takeUnless { it.isNullOrBlank() } ?: "Unknown"
    val campusName = item.campus?.name.takeUnless { it.isNullOrBlank() }
        ?: item.campus?.shortLabel.orEmpty()
    val locationText = item.location.orEmpty()

    var showClaimSheet by remember { mutableStateOf(false) }
    var secretAnswer by remember { mutableStateOf("") }
    var claimMessage by remember { mutableStateOf("") }
    var showEmailToPoster by remember { mutableStateOf(false) }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val incomingClaims = data.incomingClaims

    // Surface submit-claim errors as a toast and reset the VM error so it doesn't re-fire.
    LaunchedEffect(submitState) {
        if (submitState is SubmitClaimState.Error) {
            Toast.makeText(context, submitState.message, Toast.LENGTH_SHORT).show()
            onResetSubmit()
        }
    }

    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Item Details",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Admin edit button
                    if (isAdmin && isPoster) {
                        IconButton(onClick = {
                            Toast.makeText(context, "Edit item (Mock action)", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!isPoster) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(UniLostSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                    ) {
                        UniLostButton(
                            text = if (isFound) "This Is Mine" else "I Found This",
                            onClick = { showClaimSheet = true },
                            icon = if (isFound) Icons.Default.PanTool else Icons.Default.Search,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { showReportDialog = true },
                            shape = UniLostShapes.md
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = "Report", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                // Poster view: admin controls
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(UniLostSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                    ) {
                        UniLostButton(
                            text = "Edit Item",
                            onClick = {
                                Toast.makeText(context, "Edit item (Mock action)", Toast.LENGTH_SHORT).show()
                            },
                            variant = ButtonVariant.SECONDARY,
                            icon = Icons.Default.Edit,
                            modifier = Modifier.weight(1f)
                        )
                        UniLostButton(
                            text = "Delete",
                            onClick = { showDeleteDialog = true },
                            variant = ButtonVariant.DANGER,
                            icon = Icons.Default.Delete,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                RemoteImage(
                    url = item.imageUrls.firstOrNull(),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    blurred = isFound,
                    placeholderIconSize = 64.dp,
                )

                // Type badge
                Surface(
                    modifier = Modifier
                        .padding(UniLostSpacing.md)
                        .align(Alignment.TopStart),
                    shape = UniLostShapes.full,
                    color = if (item.type == "LOST") ErrorRed else Sage400
                ) {
                    Text(
                        item.type,
                        color = White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                if (isFound) {
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        shape = UniLostShapes.md,
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Text(
                            "Image blurred to protect the owner",
                            color = White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm)
                        )
                    }
                }

                // Image page indicator (placeholder for multi-image gallery)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = UniLostSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == 0) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == 0) White
                                    else White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                // Status chips
                Row(horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)) {
                    StatusChip(item.type)
                    StatusChip(item.status)
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                // Title
                Text(
                    item.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                // Description (expandable if long)
                val descriptionIsLong = item.description.length > 120
                if (descriptionIsLong && !descriptionExpanded) {
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    TextButton(
                        onClick = { descriptionExpanded = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Read more",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                    if (descriptionIsLong) {
                        TextButton(
                            onClick = { descriptionExpanded = false },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Show less",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.md))

                // Meta info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = UniLostShapes.md,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(UniLostSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                    ) {
                        MetaRow(Icons.Default.Category, "Category", item.category)
                        MetaRow(Icons.Default.LocationOn, "Location", locationText)
                        MetaRow(Icons.Default.CalendarToday, "Posted", timeAgo(item.createdAt))
                        MetaRow(Icons.Default.School, "School", campusName)
                    }
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.md))

                // Poster info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = UniLostShapes.md,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(UniLostSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(name = posterName, size = 40.dp)
                        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                        Column {
                            Text(
                                posterName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                campusName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Related items
                if (data.relatedItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(UniLostSpacing.lg))
                    Text(
                        "Related Items",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    data.relatedItems.forEach { relItem ->
                        RelatedItemCard(
                            item = relItem,
                            onClick = { navController.navigate("item_detail_screen/${relItem.id}") }
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    }
                }

                // Incoming Claims section (visible only for poster).
                if (isPoster && incomingClaims.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(UniLostSpacing.lg))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Incoming Claims",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = UniLostShapes.md,
                            color = PurpleBg
                        ) {
                            Text(
                                "${incomingClaims.size}",
                                color = Purple,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Only one claim can be approved per item",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningHover
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))

                    incomingClaims.forEach { claim ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("claim_detail_screen/${claim.id}") },
                            shape = UniLostShapes.md,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Claimant info
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarView(name = claim.claimantName, size = 32.dp)
                                    Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            claim.claimantName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            claim.claimantSchool.orEmpty(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    StatusChip(claim.status)
                                }

                                // Secret detail comparison (for FOUND items)
                                if (isFound && !claim.providedAnswer.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                                    ) {
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = UniLostShapes.sm,
                                            color = Sage400_8pct
                                        ) {
                                            Column(modifier = Modifier.padding(UniLostSpacing.sm)) {
                                                Text(
                                                    "YOUR DETAIL",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Sage400
                                                )
                                                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                                                Text(
                                                    item.secretDetailQuestion ?: "N/A",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = UniLostShapes.sm,
                                            color = PurpleBg
                                        ) {
                                            Column(modifier = Modifier.padding(UniLostSpacing.sm)) {
                                                Text(
                                                    "THEIR ANSWER",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Purple
                                                )
                                                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                                                Text(
                                                    claim.providedAnswer.orEmpty(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                // Message
                                Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                                Text(
                                    claim.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )

                                // Approve / Reject buttons (only for PENDING)
                                if (claim.status.equals("PENDING", ignoreCase = true)) {
                                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                                    ) {
                                        UniLostButton(
                                            text = "Reject",
                                            onClick = { onRejectIncoming(claim.id) },
                                            variant = ButtonVariant.DANGER,
                                            icon = Icons.Default.Close,
                                            modifier = Modifier.weight(1f),
                                            isCompact = true,
                                            enabled = !incomingActionInFlight
                                        )
                                        UniLostButton(
                                            text = "Approve",
                                            onClick = { onAcceptIncoming(claim.id) },
                                            variant = ButtonVariant.PRIMARY,
                                            icon = Icons.Default.Check,
                                            modifier = Modifier.weight(1f),
                                            isCompact = true,
                                            enabled = !incomingActionInFlight
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    }
                }

                Spacer(modifier = Modifier.height(UniLostSpacing.md))
            }
        }
    }

    // Report Dialog
    if (showReportDialog) {
        var selectedReason by remember { mutableStateOf("") }
        val reportReasons = listOf(
            "Spam or misleading",
            "Inappropriate content",
            "Duplicate item",
            "Suspected scam",
            "Other"
        )

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    "Report This Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(UniLostSpacing.xs)) {
                    Text(
                        "Why are you reporting this item?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    reportReasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = UniLostSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                            Text(
                                reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReportDialog = false
                        Toast.makeText(context, "Report submitted. Thank you!", Toast.LENGTH_SHORT).show()
                    },
                    enabled = selectedReason.isNotBlank(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = UniLostShapes.lg
        )
    }

    // Delete Confirmation Dialog (admin/poster only)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${item.title}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        Toast.makeText(context, "Item deleted (Mock action)", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = UniLostShapes.lg
        )
    }

    // Claim Bottom Sheet
    if (showClaimSheet) {
        val closeSheet: () -> Unit = {
            showClaimSheet = false
            onResetSubmit()
            secretAnswer = ""
            claimMessage = ""
            showEmailToPoster = false
        }
        ModalBottomSheet(
            onDismissRequest = closeSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = UniLostShapes.bottomSheet
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = UniLostSpacing.xl)
            ) {
                if (submitState is SubmitClaimState.Success) {
                    val submittedClaim = submitState.claim
                    val isAutoAccept = submittedClaim.status.equals("ACCEPTED", ignoreCase = true)
                    // Success state - different messages for LOST (auto-accept) vs FOUND
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = UniLostSpacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessBg,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(UniLostSpacing.md))
                        Text(
                            "Claim Submitted!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.xs))

                        // Different message for LOST (auto-accept) vs FOUND
                        if (isAutoAccept) {
                            Surface(
                                shape = UniLostShapes.sm,
                                color = SuccessBg,
                                modifier = Modifier.padding(horizontal = UniLostSpacing.sm)
                            ) {
                                Row(
                                    modifier = Modifier.padding(UniLostSpacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Success,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                                    Text(
                                        "Your claim was automatically accepted! A chat has been opened.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Success,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Text(
                                "The poster will review your claim and get back to you.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = UniLostSpacing.md)
                            )
                        }

                        Spacer(modifier = Modifier.height(UniLostSpacing.lg))

                        // "Open Chat" button (shown when auto-accepted and chat exists)
                        if (isAutoAccept) {
                            val chatRoute = submittedClaim.chatId ?: submittedClaim.id
                            UniLostButton(
                                text = "Open Chat",
                                onClick = {
                                    closeSheet()
                                    navController.navigate("chat_detail_screen/$chatRoute")
                                },
                                icon = Icons.Default.Chat,
                                fillWidth = false
                            )
                            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        }

                        UniLostButton(
                            text = "View My Claims",
                            onClick = {
                                closeSheet()
                                navController.navigate("my_claims_screen")
                            },
                            variant = if (isAutoAccept) ButtonVariant.SECONDARY else ButtonVariant.PRIMARY,
                            fillWidth = false
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        UniLostButton(
                            text = "Back to Item",
                            onClick = closeSheet,
                            variant = ButtonVariant.GHOST,
                            fillWidth = false
                        )
                    }
                } else {
                    // Claim form
                    Text(
                        "Submit a Claim",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Provide details to prove this item is yours.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    // Item summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = UniLostShapes.sm,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(UniLostSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = UniLostShapes.sm,
                                color = if (isFound) SuccessBg else ErrorBg,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isFound) Icons.Default.Inventory2 else Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = if (isFound) Success else ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                            Column {
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    item.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isFound) Success else ErrorRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    // Secret detail answer (FOUND items only)
                    if (isFound) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Secret Detail Answer",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                        Text(
                            "Describe a unique feature only the owner would know.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        UniLostTextField(
                            value = secretAnswer,
                            onValueChange = { secretAnswer = it },
                            placeholder = "e.g., There's a scratch on the back...",
                            singleLine = false,
                            minLines = 2,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.md))
                    }

                    // Message
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Message",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                    Text(
                        "Why do you think this item is yours?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    UniLostTextField(
                        value = claimMessage,
                        onValueChange = { if (it.length <= 500) claimMessage = it },
                        placeholder = "Describe when and where you lost it...",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5,
                        supportingText = "${claimMessage.length}/500"
                    )

                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    // "Show email to poster" checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEmailToPoster = !showEmailToPoster },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showEmailToPoster,
                            onCheckedChange = { showEmailToPoster = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                        Column {
                            Text(
                                "Show my email to the poster",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Allow the poster to contact you via email",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(UniLostSpacing.md))

                    // Submit
                    val isSubmitting = submitState is SubmitClaimState.Submitting
                    UniLostButton(
                        text = if (isSubmitting) "Submitting..." else "Submit Claim",
                        onClick = {
                            onSubmitClaim(
                                claimMessage,
                                secretAnswer.takeIf { isFound && it.isNotBlank() },
                            )
                        },
                        icon = Icons.Default.Send,
                        enabled = claimMessage.isNotBlank() && !isSubmitting
                    )
                }
            }
        }
    }
}

/**
 * Compact related item card used in item detail.
 */
@Composable
private fun RelatedItemCard(
    item: ItemDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = UniLostShapes.md,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = UniLostShapes.sm,
                color = if (item.type == "LOST") ErrorBg else SuccessBg,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (item.type == "LOST") Icons.Default.SearchOff else Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = if (item.type == "LOST") ErrorRed else Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${item.category} • ${item.location.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            StatusChip(item.type)
        }
    }
}

@Composable
fun MetaRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(UniLostSpacing.sm))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
