package com.jassi.learningdashboard.presentation.detail

import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.model.Lesson

sealed interface CourseDetailUiState {

    data object Loading : CourseDetailUiState

    data object NotFound : CourseDetailUiState

    data class Content(
        val course: Course,
        val lessons: List<Lesson>,
    ) : CourseDetailUiState
}
