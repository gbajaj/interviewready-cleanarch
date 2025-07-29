package com.gauravbajaj.interviewready.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.gauravbajaj.interviewready.components.ScreenContent
import com.gauravbajaj.interviewready.base.UIState
import com.gauravbajaj.interviewready.model.User

/**
 * Composable function for the Home Screen.
 *
 * This screen displays a list of users fetched from a ViewModel using the new
 * ApiResult-based error handling system. It handles different UI states
 * (Initial, Loading, Success, Error) with enhanced error messages and retry logic.
 *
 * @param navController The NavController used for navigation.
 * @param modifier The Modifier to be applied to the root Composable of this screen.
 * @param onItemClick A callback function that is invoked when a user item is clicked.
 *                    It receives the clicked [User] object as a parameter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (User) -> Unit = {}
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load users when the screen is first displayed
    LaunchedEffect(Unit) {
        if (uiState is UIState.Initial) {
            viewModel.loadUsers()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Interview Ready") }
            )
        }
    ) { paddingValues ->
        ScreenContent(
            uiState = uiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onRetry = { viewModel.retry() }
        ) {
            // Success state content
            val successState = uiState as UIState.Success
            val users = successState.data

            if (users.isEmpty()) {
                // Handle empty state with a nice message
                EmptyUsersContent()
            } else {
                UsersList(
                    users = users,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

/**
 * Displays the list of users
 */
@Composable
private fun UsersList(
    users: List<User>,
    onItemClick: (User) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users.size) { index ->
            val user = users[index]
            UserItem(
                user = user,
                onClick = { onItemClick(user) }
            )
        }
    }
}

/**
 * Individual user item
 */
@Composable
private fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state when no users are available
 */
@Composable
private fun EmptyUsersContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No users found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "There are currently no users to display",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}