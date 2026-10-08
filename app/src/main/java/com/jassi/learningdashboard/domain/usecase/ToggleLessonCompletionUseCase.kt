package com.jassi.learningdashboard.domain.usecase

import com.jassi.learningdashboard.domain.model.Lesson
import com.jassi.learningdashboard.domain.repository.CourseRepository
import javax.inject.Inject

class ToggleLessonCompletionUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    suspend operator fun invoke(lesson: Lesson) {
        courseRepository.setLessonCompleted(lesson.id, !lesson.isCompleted)
    }
}
