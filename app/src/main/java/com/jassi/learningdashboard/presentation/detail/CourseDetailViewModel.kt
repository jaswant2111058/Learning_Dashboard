package com.jassi.learningdashboard.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jassi.learningdashboard.domain.model.Lesson
import com.jassi.learningdashboard.domain.usecase.ObserveCourseDetailUseCase
import com.jassi.learningdashboard.domain.usecase.ToggleLessonCompletionUseCase
import com.jassi.learningdashboard.presentation.navigation.CourseDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCourseDetail: ObserveCourseDetailUseCase,
    private val toggleLessonCompletion: ToggleLessonCompletionUseCase,
) : ViewModel() {

    private val courseId: Long = savedStateHandle.toRoute<CourseDetailRoute>().courseId

    val uiState: StateFlow<CourseDetailUiState> = observeCourseDetail(courseId)
        .map { detail ->
            if (detail == null) {
                CourseDetailUiState.NotFound
            } else {
                CourseDetailUiState.Content(course = detail.course, lessons = detail.lessons)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CourseDetailUiState.Loading,
        )

    fun onLessonToggled(lesson: Lesson) {
        viewModelScope.launch { toggleLessonCompletion(lesson) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
