package com.gauravbajaj.interviewready.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays an error message with an optional retry button.
 *
 * @param message The error message to display.
 * @param onRetry A callback function to be invoked when the retry button is clicked.
 *                If null, no retry button will be shown.
 * @param modifier Optional modifier for this composable.
 */
@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Only show retry button if onRetry is provided
        onRetry?.let { retryAction ->
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = retryAction,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Try Again")
            }
        }
    }
}