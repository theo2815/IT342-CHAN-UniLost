package com.hulampay.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hulampay.mobile.ui.theme.*

/**
 * Button variant types following spec Section 8.1.
 */
enum class ButtonVariant {
    PRIMARY,     // Filled — main actions (Login, Submit, Save)
    SECONDARY,   // Outlined — secondary actions (Cancel, Register)
    DANGER,      // Filled red — destructive (Delete, Reject)
    GHOST        // Text only — tertiary actions (Skip, Learn more)
}

/**
 * UniLost design system button.
 *
 * @param text Button label text
 * @param onClick Click handler
 * @param variant One of PRIMARY, SECONDARY, DANGER, GHOST
 * @param icon Optional leading icon
 * @param isLoading Shows spinner and disables
 * @param isCompact Use 40dp height instead of 48dp
 * @param fillWidth Whether button fills available width
 */
@Composable
fun UniLostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isCompact: Boolean = false,
    fillWidth: Boolean = true
) {
    val height = if (isCompact) 40.dp else 48.dp
    val widthModifier = if (fillWidth) modifier.fillMaxWidth() else modifier

    when (variant) {
        ButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = widthModifier.height(height),
                shape = UniLostShapes.md,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = enabled && !isLoading
            ) {
                ButtonContent(text, icon, isLoading, MaterialTheme.colorScheme.onPrimary)
            }
        }

        ButtonVariant.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = widthModifier.height(height),
                shape = UniLostShapes.md,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = enabled && !isLoading
            ) {
                ButtonContent(text, icon, isLoading, MaterialTheme.colorScheme.primary)
            }
        }

        ButtonVariant.DANGER -> {
            Button(
                onClick = onClick,
                modifier = widthModifier.height(height),
                shape = UniLostShapes.md,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.outline,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = enabled && !isLoading
            ) {
                ButtonContent(text, icon, isLoading, MaterialTheme.colorScheme.onError)
            }
        }

        ButtonVariant.GHOST -> {
            TextButton(
                onClick = onClick,
                modifier = widthModifier.height(height),
                shape = UniLostShapes.md,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = enabled && !isLoading
            ) {
                ButtonContent(text, icon, isLoading, MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    isLoading: Boolean,
    spinnerColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            color = spinnerColor,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    } else {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(UniLostSpacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
