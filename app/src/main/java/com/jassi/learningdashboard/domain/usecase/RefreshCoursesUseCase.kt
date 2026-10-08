package com.jassi.learningdashboard.domain.usecase

import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.domain.repository.CourseRepository
import javax.inject.Inject

class RefreshCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = courseRepository.refreshCourses()
}
