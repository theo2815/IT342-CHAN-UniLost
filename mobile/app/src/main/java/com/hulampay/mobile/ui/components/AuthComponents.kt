package com.hulampay.mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.hulampay.mobile.R
import com.hulampay.mobile.ui.theme.*


/**
 * UniLost branded OutlinedTextField for auth forms.
 * Uses semantic color tokens from the design system.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            shape = UniLostShapes.md,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.surface,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error,
            ),
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null
        )
    }
}

/**
 * UniLost primary filled button.
 * Uses design system colors and sizing.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = UniLostShapes.md,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Shared logo header for auth screens — mirrors the website's `.logo`
 * composition (mark + "UniLost" wordmark).
 */
@Composable
fun AuthLogoHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UniLostSpacing.sm)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_unilost_logo),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = "UniLost",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
