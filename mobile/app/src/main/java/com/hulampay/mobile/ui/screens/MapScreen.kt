package com.hulampay.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.hulampay.mobile.data.model.ItemDto
import com.hulampay.mobile.data.model.School
import com.hulampay.mobile.navigation.Screen
import com.hulampay.mobile.ui.components.*
import com.hulampay.mobile.ui.theme.*
import com.hulampay.mobile.utils.UiState

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

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(CEBU_CENTER, DEFAULT_ZOOM)
    }

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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.clearCampusFilter()
                    viewModel.selectCampusMarker(null)
                },
                shape = UniLostShapes.md,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Re-center"
                )
            }
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                // Campus markers — purple pins. Tap selects the campus for the overlay card.
                val visibleCampuses = if (campusFilterId == null) campuses
                else campuses.filter { it.id == campusFilterId }
                visibleCampuses.forEach { campus ->
                    val lat = campus.centerLat ?: return@forEach
                    val lng = campus.centerLng ?: return@forEach
                    Marker(
                        state = MarkerState(position = LatLng(lat, lng)),
                        title = campus.displayName,
                        snippet = campus.name?.takeIf { it != campus.displayName },
                        icon = BitmapDescriptorFactory.defaultMarker(CAMPUS_PIN_HUE),
                        zIndex = 1f,
                        onClick = {
                            viewModel.selectCampusMarker(campus.id)
                            false
                        }
                    )
                }

                // Item markers — exact pins solid; approximate (campus-center fallback) dimmed.
                // Tap shows the Compose ItemInfoCard overlay instead of the default info window.
                markerData.forEach { md ->
                    val isLost = md.item.type.equals("LOST", ignoreCase = true)
                    Marker(
                        state = MarkerState(position = md.position),
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (isLost) BitmapDescriptorFactory.HUE_RED
                            else BitmapDescriptorFactory.HUE_GREEN
                        ),
                        alpha = if (md.isExact) 1f else 0.55f,
                        zIndex = 2f,
                        onClick = {
                            viewModel.selectItem(md.item.id)
                            true
                        }
                    )
                }
            }

            // Floating filter chips (top)
            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = UniLostSpacing.sm),
                contentPadding = PaddingValues(horizontal = UniLostSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
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

            // Active campus filter chip (under the filter row when a campus is selected)
            if (campusFilterId != null) {
                val filteredCampus = campusById[campusFilterId]
                if (filteredCampus != null) {
                    AssistChip(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 56.dp),
                        onClick = { viewModel.clearCampusFilter() },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                filteredCampus.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = UniLostShapes.full,
                        elevation = AssistChipDefaults.assistChipElevation(4.dp)
                    )
                }
            }

            // Loading / error overlays
            when (val s = state) {
                is UiState.Loading -> {
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
                is UiState.Error -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 64.dp, start = UniLostSpacing.md, end = UniLostSpacing.md)
                            .fillMaxWidth(),
                        shape = UniLostShapes.md,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            s.message,
                            modifier = Modifier.padding(UniLostSpacing.md),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                else -> Unit
            }

            // Bottom slot — priority: item card (Phase C) > campus card (Phase B) > legend (Phase A).
            when {
                selectedItem != null -> {
                    ItemInfoCard(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(UniLostSpacing.md)
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
                            .padding(UniLostSpacing.md)
                            .fillMaxWidth(),
                        campus = selectedCampus,
                        itemCount = itemCountByCampus[selectedCampus.id] ?: 0,
                        isAlreadyFiltered = campusFilterId == selectedCampus.id,
                        onClose = { viewModel.selectCampusMarker(null) },
                        onViewItems = { viewModel.viewCampusItems(selectedCampus.id) }
                    )
                }
                else -> {
                    MapLegend(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(UniLostSpacing.md),
                        lostCount = lostCount,
                        foundCount = foundCount
                    )
                }
            }
        }
    }
}

@Composable
private fun MapLegend(
    modifier: Modifier = Modifier,
    lostCount: Int,
    foundCount: Int,
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
        }
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
