package com.jassi.learningdashboard.presentation.dashboard

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jassi.learningdashboard.core.result.DataError
import com.jassi.learningdashboard.core.ui.components.CourseProgress
import com.jassi.learningdashboard.core.ui.components.EmptyState
import com.jassi.learningdashboard.core.ui.components.LoadingState
import com.jassi.learningdashboard.core.ui.components.MessageState
import com.jassi.learningdashboard.core.ui.components.OfflineBanner
import com.jassi.learningdashboard.domain.model.Course

@Composable
fun DashboardScreen(
    onCourseClick: (Long) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        onCourseClick = onCourseClick,
        onRetry = viewModel::refresh,
        onRefreshErrorDismissed = viewModel::onRefreshErrorDismissed,
        onLogout = {
            viewModel.logout()
            onLoggedOut()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onCourseClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onRefreshErrorDismissed: () -> Unit,
    onLogout: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    (uiState as? DashboardUiState.Content)?.refreshError?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error.dashboardMessage())
            onRefreshErrorDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                actions = {
                    val isRefreshing = (uiState as? DashboardUiState.Content)?.isRefreshing == true
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .height(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = onRetry) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh courses")
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                DashboardUiState.Loading -> LoadingState(label = "Loading your courses…")

                DashboardUiState.Empty -> EmptyState(
                    title = "No courses yet",
                    description = "Courses you enrol in will appear here.",
                )

                is DashboardUiState.Failure -> MessageState(
                    icon = Icons.Filled.CloudOff,
                    title = uiState.error.failureTitle(),
                    description = uiState.error.dashboardMessage(),
                    actionLabel = "Try again",
                    onAction = onRetry,
                )

                is DashboardUiState.Content -> {
                    if (uiState.isOffline) {
                        OfflineBanner(message = offlineLabel(uiState.lastSyncedAtMillis))
                    }
                    CourseList(courses = uiState.courses, onCourseClick = onCourseClick)
                }
            }
        }
    }
}

@Composable
private fun CourseList(courses: List<Course>, onCourseClick: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = courses, key = { it.id }) { course ->
            CourseCard(course = course, onClick = { onCourseClick(course.id) })
        }
    }
}

@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                text = course.instructor,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(14.dp))
            CourseProgress(
                progressPercent = course.progressPercent,
                completedLessons = course.completedLessons,
                totalLessons = course.totalLessons,
            )

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onClick) {
                    Text(if (course.isNotStarted) "Start" else "Continue")
                }
            }
        }
    }
}

private fun offlineLabel(lastSyncedAtMillis: Long?): String {
    val base = "Offline — showing saved courses"
    if (lastSyncedAtMillis == null) return base
    val relative = DateUtils.getRelativeTimeSpanString(
        lastSyncedAtMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    )
    return "$base · synced $relative"
}

private fun DataError.failureTitle(): String = when (this) {
    DataError.Network -> "You are offline"
    else -> "Could not load courses"
}

internal fun DataError.dashboardMessage(): String = when (this) {
    DataError.Network ->
        "We could not reach the server and there is nothing saved on this device yet. " +
            "Connect to the internet and try again."
    is DataError.Server -> "The server returned an error ($code). Please try again shortly."
    DataError.Serialization -> "The course data could not be read. Please update the app."
    DataError.Unauthorized -> "Your session has expired. Please log in again."
    is DataError.Unknown -> "Something went wrong while loading your courses."
}
