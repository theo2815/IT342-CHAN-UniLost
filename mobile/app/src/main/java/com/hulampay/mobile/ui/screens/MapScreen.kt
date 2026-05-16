package com.hulampay.mobile.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState
import com.hulampay.mobile.utils.timeAgo
import kotlinx.coroutines.launch

private val CEBU_CENTER = LatLng(10.3157, 123.8854)
private const val DEFAULT_ZOOM = 13f
private const val CAMPUS_ZOOM = 16f
private val CAMPUS_PIN_HUE = BitmapDescriptorFactory.HUE_VIOLET

/**
 * Map View screen — Spec Section 10.8.
 * Live Google Maps view backed by GET /api/items/map and GET /api/campuses.
 * Mirrors the website's marker set: campus pins + item pins + approximate-position fallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel(),
    badgeViewModel: NotificationBadgeViewModel = hiltViewModel(),
    chatBadgeViewModel: ChatBadgeViewModel = hiltViewModel(),
    focusLat: Double? = null,
    focusLng: Double? = null,
    focusItemId: String? = null,
) {
    val filters = listOf("All", "Lost", "Found")
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedFilterCode by viewModel.filter.collectAsStateWithLifecycle()
    val campuses by viewModel.campuses.collectAsStateWithLifecycle()
    val campusFilterId by viewModel.campusFilterId.collectAsStateWithLifecycle()
    val selectedCampusId by viewModel.selectedCampusId.collectAsStateWithLifecycle()
    val selectedItemId by viewModel.selectedItemId.collectAsStateWithLifecycle()
    val unreadNotifications by badgeViewModel.unread.collectAsState()
    val unreadChats by chatBadgeViewModel.unread.collectAsState()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val locationStatus by viewModel.locationStatus.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
            || results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermission(granted)
    }

    // On first composition, either start subscribing (already granted) or request.
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.onLocationPermission(true)
        } else if (locationStatus == LocationStatus.UNKNOWN) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    // Live location updates — bound to the GRANTED state so they auto-stop
    // when permission flips to DENIED/UNAVAILABLE and resume on grant.
    if (locationStatus == LocationStatus.GRANTED) {
        DisposableEffect(Unit) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10_000L
            ).setMinUpdateIntervalMillis(5_000L).build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    viewModel.setUserLocation(LatLng(loc.latitude, loc.longitude))
                }
            }
            try {
                client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            } catch (e: SecurityException) {
                viewModel.markLocationUnavailable()
            }
            onDispose { client.removeLocationUpdates(callback) }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(CEBU_CENTER, DEFAULT_ZOOM)
    }
    val scope = rememberCoroutineScope()

    val items: List<ItemDto> = (state as? UiState.Success)?.data.orEmpty()
    val campusById: Map<String, School> = campuses.associateBy { it.id }
    val selectedCampus: School? = selectedCampusId?.let(campusById::get)
    val selectedItem: ItemDto? = selectedItemId?.let { id -> items.firstOrNull { it.id == id } }

    // Items shown on the map — resolves coords (own lat/lng or campus-center fallback).
    data class MarkerData(
        val item: ItemDto,
        val position: LatLng,
        val isExact: Boolean,
    )
    val markerData: List<MarkerData> = items.mapNotNull { item ->
        val lat = item.latitude
        val lng = item.longitude
        if (lat != null && lng != null) {
            MarkerData(item, LatLng(lat, lng), isExact = true)
        } else {
            val campus = item.campusId?.let(campusById::get)
            val cLat = campus?.centerLat
            val cLng = campus?.centerLng
            if (cLat != null && cLng != null) {
                MarkerData(item, LatLng(cLat, cLng), isExact = false)
            } else null
        }
    }

    val markerByItemId: Map<String, MarkerData> = markerData.associateBy { it.item.id }
    val selectedMarker: MarkerData? = selectedItemId?.let(markerByItemId::get)
    val lostCount = markerData.count { it.item.type.equals("LOST", ignoreCase = true) }
    val foundCount = markerData.count { it.item.type.equals("FOUND", ignoreCase = true) }
    val itemCountByCampus: Map<String, Int> = items
        .mapNotNull { it.campusId }
        .groupingBy { it }
        .eachCount()

    // Animate camera when the campus filter changes.
    LaunchedEffect(campusFilterId, campuses) {
        val id = campusFilterId
        if (id == null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(CEBU_CENTER, DEFAULT_ZOOM)
            )
        } else {
            val c = campusById[id] ?: return@LaunchedEffect
            val lat = c.centerLat ?: return@LaunchedEffect
            val lng = c.centerLng ?: return@LaunchedEffect
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), CAMPUS_ZOOM)
            )
        }
    }

    // Auto-focus when entered via "View Location" deep-link. Pans + zooms to
    // the supplied coords once items have loaded, and selects the matching
    // item so its info card opens. Fires once per navigation entry — mirrors
    // MapView.jsx:91-113.
    val hasFocused = remember(focusLat, focusLng, focusItemId) {
        mutableStateOf(false)
    }
    LaunchedEffect(state, focusLat, focusLng, focusItemId) {
        if (hasFocused.value) return@LaunchedEffect
        if (focusLat == null || focusLng == null) return@LaunchedEffect
        if (state !is UiState.Success) return@LaunchedEffect
        hasFocused.value = true
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(LatLng(focusLat, focusLng), CAMPUS_ZOOM)
        )
        if (focusItemId != null && items.any { it.id == focusItemId }) {
            viewModel.selectItem(focusItemId)
        }
    }

    // Bottom-sheet state. Persistent (skipHidden) so the sheet always shows at
    // least its peek above the bottom nav. Starts partially-expanded so users see
    // the drag handle + count header on first composition.
    val sheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
    )
    val sheetCurrentValue = sheetScaffoldState.bottomSheetState.currentValue
    val showOverlayCard = sheetCurrentValue != SheetValue.Expanded
    val peekHeight = 96.dp

    val selectedCampusLabel: String = when {
        campusFilterId == null -> ""
        else -> campusById[campusFilterId]?.displayName.orEmpty()
    }

    Scaffold(
        topBar = {
            UniLostTopBar(
                onLogoClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onChatClick = { navController.navigate(Screen.ChatList.route) },
                notificationCount = unreadNotifications.toInt(),
                chatCount = unreadChats.toInt()
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->
        BottomSheetScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            scaffoldState = sheetScaffoldState,
            sheetPeekHeight = peekHeight,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetContentColor = MaterialTheme.colorScheme.onSurface,
            sheetTonalElevation = 0.dp,
            sheetShadowElevation = 8.dp,
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetContent = {
                MapFeedSheetContent(
                    items = items,
                    selectedItemId = selectedItemId,
                    lostCount = lostCount,
                    foundCount = foundCount,
                    showUserDot = userLocation != null,
                    state = state,
                    onItemClick = { item ->
                        viewModel.selectItem(item.id)
                        val md = markerByItemId[item.id]
                        if (md != null) {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(md.position, CAMPUS_ZOOM)
                                )
                                sheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    },
                )
            },
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    onMapClick = {
                        viewModel.selectCampusMarker(null)
                        viewModel.selectItem(null)
                    }
                ) {
                    // Campus markers — composable pins with a shortLabel chip
                    // floating above a violet teardrop. Mirrors MapView.jsx:421-442.
                    val visibleCampuses = if (campusFilterId == null) campuses
                    else campuses.filter { it.id == campusFilterId }
                    visibleCampuses.forEach { campus ->
                        val lat = campus.centerLat ?: return@forEach
                        val lng = campus.centerLng ?: return@forEach
                        val shortName = campus.shortLabel?.takeIf { it.isNotBlank() }
                            ?: campus.displayName
                        val campusPos = LatLng(lat, lng)
                        val campusMarkerState = rememberMarkerState(position = campusPos)
                        LaunchedEffect(campusPos) { campusMarkerState.position = campusPos }
                        MarkerComposable(
                            keys = arrayOf("campus-${campus.id}", shortName),
                            state = campusMarkerState,
                            title = campus.displayName,
                            snippet = campus.name?.takeIf { it != campus.displayName },
                            zIndex = 1f,
                            onClick = {
                                viewModel.selectCampusMarker(campus.id)
                                false
                            },
                        ) {
                            CampusMarkerPin(shortLabel = shortName)
                        }
                    }

                    // Item markers — tinted Place icon. Approximate (campus-center
                    // fallback) pins render at reduced alpha. Mirrors MapView.jsx:474-497.
                    markerData.forEach { md ->
                        val isLost = md.item.type.equals("LOST", ignoreCase = true)
                        val itemMarkerState = rememberMarkerState(position = md.position)
                        LaunchedEffect(md.position) { itemMarkerState.position = md.position }
                        MarkerComposable(
                            keys = arrayOf("item-${md.item.id}", isLost, md.isExact),
                            state = itemMarkerState,
                            zIndex = 2f,
                            onClick = {
                                viewModel.selectItem(md.item.id)
                                true
                            },
                        ) {
                            ItemMarkerPin(isLost = isLost, isApproximate = !md.isExact)
                        }
                    }

                    // User location — accuracy circle + pulsing blue dot. Mirrors
                    // MapView.jsx:116-147 (web uses imperative google.maps.Circle).
                    val userLoc = userLocation
                    if (userLoc != null) {
                        Circle(
                            center = userLoc,
                            radius = 80.0,
                            fillColor = Color(0x1F3B82F6),
                            strokeColor = Color(0x4D3B82F6),
                            strokeWidth = 1f,
                            zIndex = 9f,
                        )
                        val userMarkerState = rememberMarkerState(position = userLoc)
                        LaunchedEffect(userLoc) { userMarkerState.position = userLoc }
                        MarkerComposable(
                            keys = arrayOf("user-location"),
                            state = userMarkerState,
                            anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                            zIndex = 10f,
                        ) {
                            UserLocationDot()
                        }
                    }
                }

                // Top stack — campus dropdown + filter chips. Stacked in a Column
                // so the dropdown spans the screen width and the chips sit beneath.
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(
                            top = UniLostSpacing.sm,
                            start = UniLostSpacing.md,
                            end = UniLostSpacing.md,
                        ),
                    verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                ) {
                    UniLostSelectField(
                        selectedLabel = selectedCampusLabel,
                        placeholder = "All Campuses",
                        leadingIcon = Icons.Default.School,
                    ) { close ->
                        UniLostDropdownItem(
                            text = "All Campuses",
                            active = campusFilterId == null,
                            onClick = {
                                viewModel.clearCampusFilter()
                                close()
                            },
                        )
                        campuses.forEach { campus ->
                            UniLostDropdownItem(
                                text = campus.displayName,
                                active = campusFilterId == campus.id,
                                onClick = {
                                    viewModel.viewCampusItems(campus.id)
                                    close()
                                },
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                    ) {
                        items(filters) { filter ->
                            val code = filter.uppercase()
                            FilterChip(
                                selected = selectedFilterCode == code,
                                onClick = { viewModel.setFilter(code) },
                                label = {
                                    Text(
                                        filter,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = UniLostShapes.full,
                                elevation = FilterChipDefaults.filterChipElevation(4.dp)
                            )
                        }
                    }
                }

                // "Location unavailable" badge — TopEnd, shows on permission deny.
                // Mirrors MapView.jsx:561-566.
                if (locationStatus == LocationStatus.DENIED
                    || locationStatus == LocationStatus.UNAVAILABLE
                ) {
                    AssistChip(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = UniLostSpacing.sm, end = UniLostSpacing.md),
                        onClick = {},
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp),
                            )
                        },
                        label = {
                            Text(
                                "Location unavailable",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                            disabledLabelColor = MaterialTheme.colorScheme.error,
                            disabledLeadingIconContentColor = MaterialTheme.colorScheme.error,
                        ),
                        shape = UniLostShapes.full,
                        elevation = AssistChipDefaults.assistChipElevation(4.dp),
                    )
                }

                // Inline loading puck — sheet content surfaces empty / error states.
                if (state is UiState.Loading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                UniLostShapes.md
                            )
                            .padding(UniLostSpacing.md)
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Selected-item / campus card. Anchored just above the sheet peek so
                // the InfoCard never sits under the drag handle. Hidden while the
                // sheet is fully Expanded — the LazyColumn becomes the source of truth.
                if (showOverlayCard) {
                    val overlayBottom = peekHeight + UniLostSpacing.sm
                    when {
                        selectedItem != null -> {
                            ItemInfoCard(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(
                                        start = UniLostSpacing.md,
                                        end = UniLostSpacing.md,
                                        bottom = overlayBottom,
                                    )
                                    .fillMaxWidth(),
                                item = selectedItem,
                                isApproximate = selectedMarker?.isExact == false,
                                onClose = { viewModel.selectItem(null) },
                                onViewDetails = {
                                    navController.navigate("item_detail_screen/${selectedItem.id}")
                                }
                            )
                        }
                        selectedCampus != null -> {
                            CampusInfoCard(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(
                                        start = UniLostSpacing.md,
                                        end = UniLostSpacing.md,
                                        bottom = overlayBottom,
                                    )
                                    .fillMaxWidth(),
                                campus = selectedCampus,
                                itemCount = itemCountByCampus[selectedCampus.id] ?: 0,
                                isAlreadyFiltered = campusFilterId == selectedCampus.id,
                                onClose = { viewModel.selectCampusMarker(null) },
                                onViewItems = { viewModel.viewCampusItems(selectedCampus.id) }
                            )
                        }
                        else -> Unit
                    }
                }

                // My-Location FAB — pinned just above the sheet peek. Mirrors
                // MapView.jsx:549-558. Hidden when no fix is available.
                val userLoc = userLocation
                if (userLoc != null) {
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = UniLostSpacing.md,
                                bottom = peekHeight + UniLostSpacing.md,
                            ),
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(userLoc, CAMPUS_ZOOM)
                                )
                            }
                        },
                        shape = UniLostShapes.md,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Go to my location"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom-sheet body: header row (count + legend dots) + LazyColumn of slim
 * map-feed cards. Mirrors `MapView.jsx:332-385` (the website's feed-panel),
 * adapted to the phone's BottomSheetScaffold. The peek surface (~88dp tall
 * including drag handle) shows just the header; expanding the sheet reveals
 * the list.
 */
@Composable
private fun MapFeedSheetContent(
    items: List<ItemDto>,
    selectedItemId: String?,
    lostCount: Int,
    foundCount: Int,
    showUserDot: Boolean,
    state: UiState<List<ItemDto>>,
    onItemClick: (ItemDto) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row — count + inline legend dots. Sits inside the peek area.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = UniLostSpacing.md,
                    end = UniLostSpacing.md,
                    bottom = UniLostSpacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${items.size} items",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegendDot(color = ErrorRed, label = "Lost ($lostCount)")
                LegendDot(color = Success, label = "Found ($foundCount)")
                if (showUserDot) {
                    LegendDot(color = Color(0xFF3B82F6), label = "You")
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 1.dp,
        )

        // List body — fills remaining sheet height; LazyColumn drives the
        // sheet's nested-scroll once the user drags it past peek.
        when (state) {
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UniLostSpacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        shape = UniLostShapes.md,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                    ) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(UniLostSpacing.md),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UniLostSpacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    ) {
                        EmptyState(
                            icon = Icons.Default.Place,
                            title = "No items on map",
                            message = "Items posted with a map pin will appear here.",
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = UniLostSpacing.md,
                            end = UniLostSpacing.md,
                            top = UniLostSpacing.sm,
                            bottom = UniLostSpacing.lg,
                        ),
                        verticalArrangement = Arrangement.spacedBy(UniLostSpacing.sm),
                    ) {
                        items(items, key = { it.id }) { item ->
                            MapFeedCard(
                                item = item,
                                isSelected = item.id == selectedItemId,
                                onClick = { onItemClick(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slim card used inside the bottom-sheet feed. A purpose-built version of
 * `ItemFeedCard` (`ui/items/ItemFeedScreen.kt:426-589`) tuned for the sheet —
 * smaller thumbnail, single-line title, no carousel. Per R1/R6 the original
 * `ItemFeedCard` is left untouched.
 */
@Composable
private fun MapFeedCard(
    item: ItemDto,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isLost = item.type.equals("LOST", ignoreCase = true)
    val isFound = item.type.equals("FOUND", ignoreCase = true)
    val typeColor = if (isLost) ErrorRed else Sage400
    val locationText = item.location.orEmpty()
    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val containerColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        else MaterialTheme.colorScheme.surface

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = UniLostShapes.md,
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UniLostSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RemoteImage(
                url = item.imageUrls.firstOrNull(),
                contentDescription = item.title,
                modifier = Modifier
                    .size(60.dp)
                    .clip(UniLostShapes.md),
                blurred = isFound,
                placeholderIconSize = 24.dp,
            )

            Spacer(modifier = Modifier.width(UniLostSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = UniLostShapes.full,
                        color = typeColor,
                    ) {
                        Text(
                            item.type,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        timeAgo(item.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (locationText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            locationText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Campus map pin — a small short-label chip floating above a violet teardrop.
 * Mirrors the website's `.adv-campus-marker` SVG layout (MapView.jsx:429-439).
 */
@Composable
private fun CampusMarkerPin(shortLabel: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = UniLostShapes.full,
            color = Color(0xFF6366F1),
            shadowElevation = 2.dp,
        ) {
            Text(
                text = shortLabel,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = Color(0xFF6366F1),
            modifier = Modifier.size(34.dp),
        )
    }
}

/**
 * Item map pin — tinted Place icon. Approximate (campus-center fallback) pins
 * render at reduced alpha so users distinguish exact from inferred positions.
 * Mirrors `.adv-item-marker.approximate` (MapView.jsx:484-494).
 */
@Composable
private fun ItemMarkerPin(isLost: Boolean, isApproximate: Boolean) {
    val tint = if (isLost) ErrorRed else Sage400
    Icon(
        imageVector = Icons.Default.Place,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(30.dp)
            .alpha(if (isApproximate) 0.55f else 1f),
    )
}

@Composable
private fun MapLegend(
    modifier: Modifier = Modifier,
    lostCount: Int,
    foundCount: Int,
    showYou: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = UniLostShapes.full,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(color = ErrorRed, label = "Lost ($lostCount)")
            LegendDot(color = Success, label = "Found ($foundCount)")
            LegendDot(color = Color(0xFF6366F1), label = "Campus")
            if (showYou) {
                LegendDot(color = Color(0xFF3B82F6), label = "You")
            }
        }
    }
}

/**
 * Pulsing blue dot rendered as the user's live-location marker.
 * Mirrors `MapView.jsx:538-545` (.user-location-pulse + .user-location-dot).
 */
@Composable
private fun UserLocationDot() {
    val transition = rememberInfiniteTransition(label = "user-loc-pulse")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart,
        ),
        label = "alpha",
    )
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Pulse ring
        Box(
            modifier = Modifier
                .size(16.dp)
                .scale(scale)
                .alpha(alpha)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6)),
        )
        // Solid dot
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6))
                .border(2.dp, Color.White, CircleShape),
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(UniLostSpacing.xs))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ItemInfoCard(
    modifier: Modifier = Modifier,
    item: ItemDto,
    isApproximate: Boolean,
    onClose: () -> Unit,
    onViewDetails: () -> Unit,
) {
    val isLost = item.type.equals("LOST", ignoreCase = true)
    val isFound = item.type.equals("FOUND", ignoreCase = true)
    val typeColor = if (isLost) ErrorRed else Sage400
    Card(
        modifier = modifier,
        shape = UniLostShapes.lg,
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(UniLostSpacing.md)) {
            Row(verticalAlignment = Alignment.Top) {
                RemoteImage(
                    url = item.imageUrls.firstOrNull(),
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(UniLostShapes.md),
                    blurred = isFound,
                    placeholderIconSize = 28.dp,
                )

                Spacer(modifier = Modifier.width(UniLostSpacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = UniLostShapes.full,
                            color = typeColor
                        ) {
                            Text(
                                item.type,
                                color = White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        if (isApproximate) {
                            Spacer(modifier = Modifier.width(UniLostSpacing.xs))
                            Surface(
                                shape = UniLostShapes.full,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    "Approximate",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    item.location?.takeIf { it.isNotBlank() }?.let { loc ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                loc,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                shape = UniLostShapes.md
            ) {
                Text("View Details")
            }
        }
    }
}

@Composable
private fun CampusInfoCard(
    modifier: Modifier = Modifier,
    campus: School,
    itemCount: Int,
    isAlreadyFiltered: Boolean,
    onClose: () -> Unit,
    onViewItems: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = UniLostShapes.lg,
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(UniLostSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFF6366F1)
                    )
                }
                Spacer(modifier = Modifier.width(UniLostSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        campus.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    campus.name?.takeIf { it.isNotBlank() && it != campus.displayName }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            campus.address?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(UniLostSpacing.xs))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(UniLostSpacing.xs))
            Text(
                "$itemCount pinned ${if (itemCount == 1) "item" else "items"} on map",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(UniLostSpacing.sm))
            Button(
                onClick = onViewItems,
                enabled = !isAlreadyFiltered,
                modifier = Modifier.fillMaxWidth(),
                shape = UniLostShapes.md
            ) {
                Text(if (isAlreadyFiltered) "Showing this campus" else "View Campus Items")
            }
        }
    }
}
