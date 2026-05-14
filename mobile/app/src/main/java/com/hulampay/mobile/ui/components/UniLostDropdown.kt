package com.hulampay.mobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.hulampay.mobile.ui.theme.UniLostShapes
import com.hulampay.mobile.ui.theme.UniLostSpacing

/**
 * UniLost reusable dropdown menu primitives — mirrors the website's `Dropdown` component
 * (`website/src/shared/components/ui/Dropdown/Dropdown.jsx`).
 *
 * Two ways to use:
 *
 * 1. **Select-style field** — for filters and form selects (most common):
 *    ```
 *    UniLostSelectField(
 *        selectedLabel = activeLabel,
 *        placeholder = "All Categories",
 *        leadingIcon = Icons.Default.Category,
 *    ) { close ->
 *        UniLostDropdownItem("All Categories", active = !active, onClick = { onChange(""); close() })
 *        items.forEach { opt ->
 *            UniLostDropdownItem(opt.label, active = opt.id == active, onClick = { onChange(opt.id); close() })
 *        }
 *    }
 *    ```
 *
 * 2. **Custom trigger + menu** — for user menus, action menus:
 *    ```
 *    var expanded by remember { mutableStateOf(false) }
 *    Box {
 *        IconButton(onClick = { expanded = true }) { Icon(...) }
 *        UniLostDropdown(expanded, onDismissRequest = { expanded = false }) {
 *            UniLostDropdownHeader("Name", subtitle = "email")
 *            UniLostDropdownDivider()
 *            UniLostDropdownItem("Profile", leadingIcon = Icons.Default.Person, onClick = { ... })
 *            UniLostDropdownItem("Logout", leadingIcon = Icons.Default.Logout,
 *                variant = UniLostDropdownItemVariant.DANGER, onClick = { ... })
 *        }
 *    }
 *    ```
 */

enum class UniLostDropdownItemVariant { DEFAULT, DANGER }

/**
 * Bare dropdown menu — wraps Material3 [DropdownMenu] with the UniLost shape and surface color.
 * Use this when you need full control over the trigger.
 */
@Composable
fun UniLostDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 4.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.background(MaterialTheme.colorScheme.surface, UniLostShapes.md),
        offset = offset,
        content = content,
    )
}

/**
 * Menu item — supports leading/trailing icons, an active (selected) state, and a danger variant.
 *
 * @param text Item label
 * @param active Highlights with primary-tinted background + primary text (matches website `.ui-dropdown-item.active`)
 * @param variant DEFAULT or DANGER (red — for destructive actions like Logout)
 */
@Composable
fun UniLostDropdownItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    active: Boolean = false,
    enabled: Boolean = true,
    variant: UniLostDropdownItemVariant = UniLostDropdownItemVariant.DEFAULT,
) {
    val isDanger = variant == UniLostDropdownItemVariant.DANGER
    val contentColor = when {
        isDanger -> MaterialTheme.colorScheme.error
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val bgColor = when {
        active && !isDanger -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onClick = onClick,
        modifier = modifier.background(bgColor),
        leadingIcon = leadingIcon?.let { iv ->
            {
                Icon(
                    imageVector = iv,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        trailingIcon = trailingIcon?.let { iv ->
            {
                Icon(
                    imageVector = iv,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        enabled = enabled,
    )
}

/**
 * Thin divider — mirrors the website's `.ui-dropdown-divider`.
 */
@Composable
fun UniLostDropdownDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

/**
 * Header section — used for user-menu cards (name + email + role badge).
 * Mirrors the website's `Dropdown.Header`.
 */
@Composable
fun UniLostDropdownHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (badge != null) {
            Spacer(Modifier.height(4.dp))
            badge()
        }
    }
}

/**
 * Select-style trigger field that opens a dropdown menu on tap.
 * The most common pattern (filters, form selects) — equivalent to the website's
 * `<Dropdown trigger={(isOpen) => <div className="ui-select-group">...</div>}>` usage.
 *
 * @param selectedLabel Label shown in the field (use `""` to show [placeholder] in muted color).
 * @param leadingIcon Optional icon shown before the label (e.g., School icon for campus filters).
 * @param content Slot for the menu items. Receives a `close` callback to dismiss after selection.
 */
@Composable
fun UniLostSelectField(
    selectedLabel: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    placeholder: String = "Select...",
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron-rotation",
    )
    val isPlaceholder = selectedLabel.isBlank()

    Box(modifier = modifier) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    onClick()
                    onExpandedChange(true)
                },
            shape = UniLostShapes.md,
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UniLostSpacing.md, vertical = UniLostSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(UniLostSpacing.sm))
                }
                Text(
                    text = if (isPlaceholder) placeholder else selectedLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPlaceholder) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
        }
        UniLostDropdown(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            content { onExpandedChange(false) }
        }
    }
}

/**
 * Convenience overload of [UniLostSelectField] that manages its own expanded state
 * (no need to hoist `expanded`).
 */
@Composable
fun UniLostSelectField(
    selectedLabel: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Select...",
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    UniLostSelectField(
        selectedLabel = selectedLabel,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        enabled = enabled,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        content = content,
    )
}
