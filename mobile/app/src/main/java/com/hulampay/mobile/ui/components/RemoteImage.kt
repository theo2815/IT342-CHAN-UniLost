package com.hulampay.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Loads [url] into a Box matching the existing UniLost item-image placeholder convention
 * (surfaceVariant background, centered Image icon when blank/loading). Falls back to the
 * placeholder when [url] is null/blank or fails. Pass [blurred] to apply the FOUND-blur
 * convention used elsewhere in the app.
 *
 * Implementation notes:
 * - Uses [AsyncImage] (NOT SubcomposeAsyncImage) so the composable is safe inside parents
 *   that perform intrinsic measurement (e.g. Row(IntrinsicSize.Min) in FeedItemCard).
 * - Avoids reading [coil.compose.AsyncImagePainter.state] in composition because doing so
 *   from inside a LazyColumn item races with [LazyLayoutPrefetcher] and corrupts the slot
 *   table. Instead, the placeholder is rendered behind unconditionally and AsyncImage draws
 *   transparently while loading / on error (no placeholder painter passed).
 */
@Composable
fun RemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    blurred: Boolean = false,
    placeholderIconSize: Dp = 28.dp,
) {
    val blurModifier = if (blurred) Modifier.blur(20.dp) else Modifier

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(placeholderIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            )
        }
    }
}
