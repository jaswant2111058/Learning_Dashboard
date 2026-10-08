package com.jassi.learningdashboard.presentation.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jassi.learningdashboard.core.ui.components.CourseProgress
import com.jassi.learningdashboard.core.ui.components.LoadingState
import com.jassi.learningdashboard.core.ui.components.MessageState
import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.model.Lesson

@Composable
fun CourseDetailScreen(
    onBack: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CourseDetailContent(
        uiState = uiState,
        onBack = onBack,
        onLessonToggled = viewModel::onLessonToggled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailContent(
    uiState: CourseDetailUiState,
    onBack: () -> Unit,
    onLessonToggled: (Lesson) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (uiState as? CourseDetailUiState.Content)?.course?.title
                            ?: "Course",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                CourseDetailUiState.Loading -> LoadingState(label = "Loading course…")

                CourseDetailUiState.NotFound -> MessageState(
                    icon = Icons.Outlined.SearchOff,
                    title = "Course unavailable",
                    description = "This course is not saved on this device. Refresh the " +
                        "dashboard while online to load it.",
                    actionLabel = "Go back",
                    onAction = onBack,
                )

                is CourseDetailUiState.Content -> {
                    CourseHeader(uiState.course)
                    HorizontalDivider()
                    LessonList(
                        lessons = uiState.lessons,
                        onLessonToggled = onLessonToggled,
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseHeader(course: Course) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
        Column(Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                text = course.instructor,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            CourseProgress(
                progressPercent = course.progressPercent,
                completedLessons = course.completedLessons,
                totalLessons = course.totalLessons,
            )
        }
    }
}

@Composable
private fun LessonList(lessons: List<Lesson>, onLessonToggled: (Lesson) -> Unit) {
    if (lessons.isEmpty()) {
        MessageState(
            icon = Icons.Outlined.Circle,
            title = "No lessons yet",
            description = "This course has no published lessons.",
        )
        return
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(items = lessons, key = { it.id }) { lesson ->
            LessonRow(lesson = lesson, onToggle = { onLessonToggled(lesson) })
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, onToggle: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (lesson.isCompleted) TextDecoration.LineThrough else null,
                color = if (lesson.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (lesson.isCompleted) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.Circle
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (lesson.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (lesson.isCompleted) "Completed" else "Pending",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
        leadingContent = {
            Text(
                text = "${lesson.position + 1}".padStart(2, '0'),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Checkbox(
                checked = lesson.isCompleted,
                onCheckedChange = { onToggle() },
            )
        },
    )
}
