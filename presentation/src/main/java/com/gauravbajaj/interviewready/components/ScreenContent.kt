package com.gauravbajaj.interviewready.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gauravbajaj.interviewready.base.UIState

/**
 * A composable function that displays different UI based on the [UIState].
 *
 * This function handles the common UI states: Initial, Loading, Success, and Error.
 * It provides enhanced error handling with retry capabilities and user-friendly messages.
 *
 * @param T The type of data associated with the [UIState].
 * @param uiState The current state of the UI.
 * @param modifier The modifier to be applied to the root Box. Defaults to [Modifier].
 * @param onRetry A lambda function to be invoked when the user clicks the retry button in the error state. Defaults to an empty lambda.
 * @param content A composable lambda that defines the UI to be displayed when the [uiState] is [UIState.Success].
 */
@Composable
fun <T> ScreenContent(
    uiState: UIState<T>,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            UIState.Initial -> {
                Text(
                    text = "Ready to load content",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            UIState.Loading -> {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is UIState.Success -> {
                content()
            }

            is UIState.Error -> {
                ErrorMessage(
                    message = uiState.message,
                    onRetry = if (uiState.canRetry) onRetry else null,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}