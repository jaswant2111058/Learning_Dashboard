package com.jassi.learningdashboard.domain.usecase

import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(): Flow<List<Course>> =
        courseRepository.observeCourses().map { courses ->
            courses.sortedWith(
                compareBy<Course> { it.displayBucket }
                    .thenByDescending { it.progressPercent }
                    .thenBy { it.title },
            )
        }

    private val Course.displayBucket: Int
        get() = when {
            isComplete -> 2
            isNotStarted -> 1
            else -> 0
        }
}
