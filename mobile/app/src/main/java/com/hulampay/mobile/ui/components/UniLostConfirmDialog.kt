package com.hulampay.mobile.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.hulampay.mobile.ui.theme.UniLostShapes

/**
 * Confirmation dialog matching the website's ConfirmDialog component.
 *
 * @param confirmVariant Use [ButtonVariant.PRIMARY] for safe actions, [ButtonVariant.DANGER]
 *   for destructive ones. The website's "warning" / "success" / "danger" variants collapse
 *   to PRIMARY + DANGER on mobile since the design system doesn't define a warning button.
 */
@Composable
fun UniLostConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cancelLabel: String = "Cancel",
    confirmVariant: ButtonVariant = ButtonVariant.PRIMARY,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            UniLostButton(
                text = confirmLabel,
                onClick = onConfirm,
                variant = confirmVariant,
                fillWidth = false,
                isCompact = true,
            )
        },
        dismissButton = {
            UniLostButton(
                text = cancelLabel,
                onClick = onDismiss,
                variant = ButtonVariant.SECONDARY,
                fillWidth = false,
                isCompact = true,
            )
        },
        shape = UniLostShapes.lg,
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
