package com.hulampay.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.hulampay.mobile.ui.theme.*

// ── UniLostTextField ──────────────────────────────────────────────────────────
/**
 * Standard text field for all UniLost forms.
 *
 * Features:
 * - Leading icon (required)
 * - Show/hide toggle when [isPassword] is true
 * - Error state: red border + helper text below
 * - Fully themed via MaterialTheme color scheme
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniLostTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val visualTransformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val isError = !errorMessage.isNullOrEmpty()

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = when {
                isPassword -> ({
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                })
                trailingContent != null -> trailingContent
                else -> null
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
        )
        if (isError) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

// ── UniLostButton — primary filled ───────────────────────────────────────────
@Composable
fun UniLostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        enabled = enabled && !isLoading,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ── UniLostOutlinedButton — secondary outlined ────────────────────────────────
@Composable
fun UniLostOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        enabled = enabled,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// ── OrDivider ─────────────────────────────────────────────────────────────────
@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
        Text(
            text = "  or  ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
    }
}

// ── Backward-compat wrappers ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = Slate400) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Slate600,
            unfocusedBorderColor = Slate200,
            cursorColor = Slate600,
            containerColor = White,
        ),
        visualTransformation = visualTransformation,
        singleLine = true,
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) = UniLostButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
)
