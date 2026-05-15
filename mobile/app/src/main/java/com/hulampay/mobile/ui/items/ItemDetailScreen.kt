package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo
import kotlinx.coroutines.delay

/**
 * Backend reason codes (matches AdminReports.jsx) → user-facing labels.
 * Order matches the website report modal for parity.
 */
private val REPORT_REASONS = listOf(
    "SPAM" to "Spam or misleading",
    "INAPPROPRIATE" to "Inappropriate content",
    "FAKE" to "Fake or suspicious",
    "DUPLICATE" to "Duplicate item",
)

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
    val flagState by viewModel.flagState.collectAsState()
    val appealState by viewModel.appealState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

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
            flagState = flagState,
            appealState = appealState,
            deleteState = deleteState,
            onSubmitClaim = viewModel::submitClaim,
            onResetSubmit = viewModel::resetSubmitState,
            onAcceptIncoming = viewModel::acceptIncomingClaim,
            onRejectIncoming = viewModel::rejectIncomingClaim,
            onFlagItem = viewModel::flagItem,
            onResetFlag = viewModel::resetFlagState,
            onSubmitAppeal = viewModel::submitAppeal,
            onResetAppeal = viewModel::resetAppealState,
            onDeleteItem = viewModel::deleteItem,
            onResetDelete = viewModel::resetDeleteState,
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
    flagState: FlagSubmitState,
    appealState: AppealSubmitState,
    deleteState: DeleteState,
    onSubmitClaim: (message: String, providedAnswer: String?) -> Unit,
    onResetSubmit: () -> Unit,
    onAcceptIncoming: (claimId: String) -> Unit,
    onRejectIncoming: (claimId: String) -> Unit,
    onFlagItem: (reason: String, description: String?) -> Unit,
    onResetFlag: () -> Unit,
    onSubmitAppeal: (text: String) -> Unit,
    onResetAppeal: () -> Unit,
    onDeleteItem: () -> Unit,
    onResetDelete: () -> Unit,
) {
    val item = data.item
    val context = LocalContext.current

    // Owner / admin view of a soft-deleted item — render a dedicated removed-state
    // screen instead of the regular detail body. Non-owners 404 at the API level.
    if (item.isDeleted == true) {
        RemovedItemScaffold(navController, item)
        return
    }

    val isFound = item.type == "FOUND"
    val isPoster = data.isPoster
    val isHidden = item.status == "HIDDEN"
    // Resolved items (RETURNED / COMPLETED) render in a read-only state: grayscale
    // hero, green banner, hidden claim/report CTAs. Mirrors website ItemDetail.jsx:199.
    val isResolved = item.status == "RETURNED" || item.status == "COMPLETED"
    val viewerHasFlagged = item.viewerHasFlagged == true

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
    var showMyReportDialog by remember { mutableStateOf(false) }
    var showAppealDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val incomingClaims = data.incomingClaims

    // Surface submit-claim errors as a toast and reset the VM error so it doesn't re-fire.
    LaunchedEffect(submitState) {
        if (submitState is SubmitClaimState.Error) {
            Toast.makeText(context, submitState.message, Toast.LENGTH_SHORT).show()
            onResetSubmit()
        }
    }

    // Report submit feedback: on Success, leave the dialog open so the inline green
    // check-icon state renders, then auto-close after 2s (mirrors website ItemDetail.jsx:238-243).
    // On Error, toast and reset; the dialog stays open so the reporter can retry.
    LaunchedEffect(flagState) {
        when (val s = flagState) {
            is FlagSubmitState.Success -> {
                delay(2000)
                showReportDialog = false
                onResetFlag()
            }
            is FlagSubmitState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
                onResetFlag()
            }
            else -> Unit
        }
    }

    // Appeal submit feedback: same pattern as flag.
    LaunchedEffect(appealState) {
        when (val s = appealState) {
            is AppealSubmitState.Success -> {
                showAppealDialog = false
                Toast.makeText(context, "Appeal submitted.", Toast.LENGTH_SHORT).show()
                onResetAppeal()
            }
            is AppealSubmitState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
                onResetAppeal()
            }
            else -> Unit
        }
    }

    // Delete feedback: close dialog + pop back on success; toast + keep dialog open on error.
    LaunchedEffect(deleteState) {
        when (val s = deleteState) {
            is DeleteState.Success -> {
                showDeleteDialog = false
                Toast.makeText(context, "Item deleted.", Toast.LENGTH_SHORT).show()
                onResetDelete()
                navController.popBackStack()
            }
            is DeleteState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
                onResetDelete()
            }
            else -> Unit
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
                    // Owner edit shortcut — disabled while the item is hidden by moderation.
                    if (isPoster) {
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.PostItem.createRoute(item.id))
                            },
                            enabled = !isHidden,
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = if (isHidden) "Edit (disabled while hidden)" else "Edit",
                                tint = if (isHidden) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Resolved items render read-only — no claim/edit/delete/report CTAs.
            // Mirrors website ItemDetail.jsx:336-495 (`isResolved` branch hides the
            // claim form and standard action row).
            if (isResolved) {
                // no bar
            } else if (!isPoster) {
                // Claimant CTA branches mirror website ItemDetail.jsx:658-671:
                //   existingClaim w/ chatId → "Open Chat" → ChatDetail
                //   existingClaim w/o chatId → "View Claim" → ClaimDetail
                //   no claim & ACTIVE → existing submit-claim sheet
                //   otherwise → no bottom bar
                val viewerClaim = data.viewerClaim
                val showSubmitCta = viewerClaim == null && item.status == "ACTIVE"
                val showBar = viewerClaim != null || showSubmitCta
                if (showBar) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(UniLostSpacing.md),
                            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
                        ) {
                            when {
                                viewerClaim?.chatId?.isNotBlank() == true -> {
                                    val chatId = viewerClaim.chatId
                                    UniLostButton(
                                        text = "Open Chat",
                                        onClick = {
                                            navController.navigate("chat_detail_screen/$chatId")
                                        },
                                        icon = Icons.Default.Chat,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                viewerClaim != null -> {
                                    UniLostButton(
                                        text = "View Claim",
                                        onClick = {
                                            navController.navigate("claim_detail_screen/${viewerClaim.id}")
                                        },
                                        icon = Icons.Default.Description,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                else -> {
                                    UniLostButton(
                                        text = if (isFound) "This Is Mine" else "I Found This",
                                        onClick = { showClaimSheet = true },
                                        icon = if (isFound) Icons.Default.PanTool else Icons.Default.Search,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    if (viewerHasFlagged) showMyReportDialog = true
                                    else showReportDialog = true
                                },
                                shape = UniLostShapes.md
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = if (viewerHasFlagged) "View my report" else "Report",
                                    modifier = Modifier.size(18.dp),
                                    tint = if (viewerHasFlagged) Warning else LocalContentColor.current,
                                )
                            }
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
                                navController.navigate(Screen.PostItem.createRoute(item.id))
                            },
                            variant = ButtonVariant.SECONDARY,
                            icon = Icons.Default.Edit,
                            modifier = Modifier.weight(1f),
                            enabled = !isHidden,
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
            // Hidden-item owner banner — appears above the hero when an admin has
            // hidden the item and the viewer is the owner. Drives the appeal flow.
            if (isPoster && isHidden) {
                HiddenItemBanner(
                    reason = item.adminActionReason,
                    actionAt = item.adminActionAt,
                    appealStatus = item.appealStatus,
                    appealText = item.appealText,
                    appealedAt = item.appealedAt,
                    appealResolvedAt = item.appealResolvedAt,
                    appealAdminNote = item.appealAdminNote,
                    onSubmitAppealClick = { showAppealDialog = true },
                )
            }

            // Image gallery — HorizontalPager over imageUrls. Single image renders as
            // a one-page pager (no dots). Grayscale tint when resolved, blur on FOUND.
            ItemImagePager(
                imageUrls = item.imageUrls,
                contentDescription = item.title,
                isFound = isFound,
                isResolved = isResolved,
                typeLabel = item.type,
                typeColor = if (item.type == "LOST") ErrorRed else Sage400,
            )

            Column(modifier = Modifier.padding(UniLostSpacing.md)) {
                // Resolved banner — only when status is RETURNED or COMPLETED.
                // Mirrors website ItemDetail.jsx:379-388.
                if (isResolved) {
                    ResolvedItemBanner(status = item.status)
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                }

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
                        // Resolved timestamp — only surfaced for RETURNED/COMPLETED items
                        // so the read-only history view shows when the handover wrapped up.
                        if (isResolved && !item.updatedAt.isNullOrBlank()) {
                            MetaRow(Icons.Default.CheckCircle, "Resolved", timeAgo(item.updatedAt))
                        }
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

                // Resolved-state inline action: View Location. The resolved branch
                // hides the bottom bar (Phase 3), so this is the only entry point to
                // the map for a returned/completed item. Mirrors website
                // ItemDetail.jsx:447-457 (resolved-actions row).
                if (isResolved && item.latitude != null && item.longitude != null) {
                    Spacer(modifier = Modifier.height(UniLostSpacing.md))
                    UniLostButton(
                        text = "View Location",
                        onClick = {
                            navController.navigate(
                                Screen.Map.createRoute(
                                    lat = item.latitude,
                                    lng = item.longitude,
                                    itemId = item.id,
                                )
                            )
                        },
                        variant = ButtonVariant.SECONDARY,
                        icon = Icons.Default.LocationOn,
                        modifier = Modifier.fillMaxWidth(),
                    )
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

    // Report Dialog — wired to POST /api/items/{id}/flag.
    if (showReportDialog) {
        var selectedCode by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        val isSubmitting = flagState is FlagSubmitState.Submitting
        val isSuccess = flagState is FlagSubmitState.Success

        AlertDialog(
            onDismissRequest = { if (!isSubmitting && !isSuccess) showReportDialog = false },
            title = {
                if (!isSuccess) {
                    Text(
                        "Report This Item",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                if (isSuccess) {
                    // Inline success state — green check + confirmation copy.
                    // Auto-dismisses after 2s via the LaunchedEffect(flagState) above.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = UniLostSpacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessBg,
                            modifier = Modifier.size(64.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(UniLostSpacing.md))
                        Text(
                            "Report submitted",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                        Text(
                            "Thanks — an admin will review it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(UniLostSpacing.xs)) {
                        Text(
                            "Why are you reporting this item?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        REPORT_REASONS.forEach { (code, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isSubmitting) { selectedCode = code }
                                    .padding(vertical = UniLostSpacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCode == code,
                                    onClick = { selectedCode = code },
                                    enabled = !isSubmitting,
                                )
                                Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 280) description = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Add a short note (optional)") },
                            minLines = 2,
                            maxLines = 4,
                            enabled = !isSubmitting,
                            shape = UniLostShapes.sm,
                        )
                        Text(
                            "${description.length} / 280",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                }
            },
            confirmButton = {
                if (!isSuccess) {
                    TextButton(
                        onClick = { onFlagItem(selectedCode, description.takeIf { it.isNotBlank() }) },
                        enabled = selectedCode.isNotBlank() && !isSubmitting,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            if (isSubmitting) "Submitting..." else "Submit Report",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
            dismissButton = if (!isSuccess) {
                {
                    TextButton(
                        onClick = { showReportDialog = false },
                        enabled = !isSubmitting,
                    ) {
                        Text("Cancel")
                    }
                }
            } else null,
            shape = UniLostShapes.lg
        )
    }

    // View My Report — shows the reporter their own submitted report + the item state.
    if (showMyReportDialog) {
        val detail = item.viewerFlagDetail
        val reasonLabel = REPORT_REASONS.firstOrNull { it.first == detail?.reason }?.second
            ?: detail?.reason.orEmpty()
        AlertDialog(
            onDismissRequest = { showMyReportDialog = false },
            title = {
                Text(
                    "Your report",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)) {
                    if (reasonLabel.isNotBlank()) {
                        Surface(
                            shape = UniLostShapes.full,
                            color = WarningBg,
                        ) {
                            Text(
                                reasonLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = WarningHover,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    val desc = detail?.description
                    if (!desc.isNullOrBlank()) {
                        Text(
                            "\"$desc\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        "Submitted ${timeAgo(detail?.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Divider()
                    if (isHidden && !item.adminActionReason.isNullOrBlank()) {
                        Text(
                            "An admin hid this item:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = WarningHover,
                        )
                        Text(
                            item.adminActionReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    } else if (isHidden) {
                        Text(
                            "An admin hid this item.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarningHover,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Text(
                            "Under review",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMyReportDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            shape = UniLostShapes.lg
        )
    }

    // Appeal Dialog — owner submits an appeal against a HIDDEN moderation.
    if (showAppealDialog) {
        var appealText by remember { mutableStateOf("") }
        val isSubmitting = appealState is AppealSubmitState.Submitting
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showAppealDialog = false },
            title = {
                Text(
                    "Appeal this hide",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(UniLostSpacing.xs)) {
                    Text(
                        "Tell the admin why this item should be restored. Be specific — they'll review it before deciding.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.sm))
                    OutlinedTextField(
                        value = appealText,
                        onValueChange = { if (it.length <= 500) appealText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Explain why this should be restored") },
                        minLines = 4,
                        maxLines = 8,
                        enabled = !isSubmitting,
                        shape = UniLostShapes.sm,
                    )
                    Text(
                        "${appealText.length} / 500",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onSubmitAppeal(appealText.trim()) },
                    enabled = appealText.isNotBlank() && !isSubmitting,
                ) {
                    Text(
                        if (isSubmitting) "Submitting..." else "Submit Appeal",
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAppealDialog = false },
                    enabled = !isSubmitting,
                ) {
                    Text("Cancel")
                }
            },
            shape = UniLostShapes.lg
        )
    }

    // Delete Confirmation Dialog (owner-only) — wired to DELETE /api/items/{id}.
    if (showDeleteDialog) {
        val isDeleting = deleteState is DeleteState.InProgress
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = {
                Text(
                    "Delete Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${item.title}\"? This action cannot be undone. " +
                        "All claims and messages associated with this item will be removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onDeleteItem() },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        if (isDeleting) "Deleting..." else "Delete",
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting,
                ) {
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

/**
 * Amber banner shown to the owner when an admin has hidden the item.
 * Renders the moderation reason + the current appeal state, with a "Submit Appeal"
 * CTA when no appeal is pending.
 */
@Composable
private fun HiddenItemBanner(
    reason: String?,
    actionAt: String?,
    appealStatus: String?,
    appealText: String?,
    appealedAt: String?,
    appealResolvedAt: String?,
    appealAdminNote: String?,
    onSubmitAppealClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(UniLostSpacing.md),
        shape = UniLostShapes.md,
        color = WarningBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Warning),
    ) {
        Column(modifier = Modifier.padding(UniLostSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = WarningHover,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                Text(
                    "This item is hidden by an admin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WarningHover,
                )
            }
            if (!reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                Text(
                    reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (!actionAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    timeAgo(actionAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
            Divider(color = Warning.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(UniLostSpacing.sm))

            when (appealStatus) {
                "PENDING" -> {
                    Text(
                        "Appeal under review · Submitted ${timeAgo(appealedAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = WarningHover,
                    )
                    if (!appealText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                        Text(
                            "\"${appealText}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                "REJECTED" -> {
                    Text(
                        "Your appeal was not approved · ${timeAgo(appealResolvedAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ErrorRed,
                    )
                    Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                    Text(
                        if (!appealAdminNote.isNullOrBlank()) "Admin note: $appealAdminNote"
                        else "No reason was given.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    // NONE / null — offer the appeal CTA.
                    UniLostButton(
                        text = "Submit Appeal",
                        onClick = onSubmitAppealClick,
                        variant = ButtonVariant.PRIMARY,
                        icon = Icons.Default.Gavel,
                        modifier = Modifier.fillMaxWidth(),
                        isCompact = true,
                    )
                }
            }
        }
    }
}

/**
 * Image hero used at the top of the detail page. Wraps [HorizontalPager] over
 * [imageUrls] (single image renders as a one-page pager with no dots). Reuses
 * the existing FOUND blur convention and overlays the type badge / blur banner /
 * page dots on top of the pager so they don't scroll with the images.
 *
 * Resolved items (RETURNED / COMPLETED) get a desaturating [ColorMatrix] filter
 * (saturation = 0.6) to mirror the website's `filter: grayscale(30%)` treatment.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemImagePager(
    imageUrls: List<String>,
    contentDescription: String,
    isFound: Boolean,
    isResolved: Boolean,
    typeLabel: String,
    typeColor: Color,
) {
    // Fall back to a one-page "empty" pager so the placeholder + overlays still render
    // when the item has no images. The pager state's pageCount stays >= 1.
    val pages: List<String?> = if (imageUrls.isEmpty()) listOf(null) else imageUrls
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val grayscaleFilter: ColorFilter? = if (isResolved) {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.4f) })
    } else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            RemoteImage(
                url = pages[page],
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                blurred = isFound,
                placeholderIconSize = 64.dp,
                colorFilter = grayscaleFilter,
            )
        }

        // Type badge (top-start)
        Surface(
            modifier = Modifier
                .padding(UniLostSpacing.md)
                .align(Alignment.TopStart),
            shape = UniLostShapes.full,
            color = typeColor
        ) {
            Text(
                typeLabel,
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

        // Dynamic page dots (hidden when only one image).
        if (pages.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = UniLostSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(pages.size) { index ->
                    val isActive = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) White
                                else White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Green banner shown at the top of the detail body for RETURNED / COMPLETED items.
 * Read-only history marker. Mirrors website ItemDetail.jsx:379-388.
 */
@Composable
private fun ResolvedItemBanner(status: String) {
    val title = if (status == "RETURNED") "Item Successfully Returned"
                else "Item Marked as Completed"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = UniLostShapes.md,
        color = SuccessBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Success),
    ) {
        Row(
            modifier = Modifier.padding(UniLostSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Success,
                )
                Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                Text(
                    "This item has been resolved and is no longer active.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Full-screen replacement shown to the owner / admin when the item has been
 * soft-deleted (item.isDeleted == true). Mirrors the website's removed-state modal.
 * Non-owners 404 at the API and never reach this branch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemovedItemScaffold(navController: NavController, item: ItemDto) {
    // Mirrors website ItemDetail.jsx:133-194 — split copy by who removed the item.
    // adminActionType == "DELETED" means an admin force-deleted it; otherwise the
    // owner deleted their own listing and the admin reason fields will be blank.
    val adminRemoved = item.adminActionType == "DELETED"
    val headline = if (adminRemoved) "An admin removed \"${item.title}\""
                   else "This listing has been removed"
    Scaffold(
        topBar = {
            UniLostDetailTopBar(
                title = "Item removed",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(UniLostSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = UniLostShapes.full,
                color = WarningBg,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(UniLostSpacing.md))
            Text(
                headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
            if (adminRemoved) {
                Text(
                    if (!item.adminActionReason.isNullOrBlank())
                        "Reason: ${item.adminActionReason}"
                    else
                        "No reason was given.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!item.adminActionAt.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(UniLostSpacing.xxs))
                    Text(
                        timeAgo(item.adminActionAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    "This listing has been deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(UniLostSpacing.lg))
            UniLostButton(
                text = "Go back",
                onClick = { navController.popBackStack() },
                variant = ButtonVariant.PRIMARY,
                icon = Icons.Default.ArrowBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
