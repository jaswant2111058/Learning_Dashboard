package com.jassi.learningdashboard.domain.usecase

import com.jassi.learningdashboard.domain.model.CourseDetail
import com.jassi.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCourseDetailUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(courseId: Long): Flow<CourseDetail?> =
        courseRepository.observeCourseDetail(courseId)
}
