package com.jassi.learningdashboard.domain.repository

import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.model.CourseDetail
import kotlinx.coroutines.flow.Flow

interface CourseRepository {

    fun observeCourses(): Flow<List<Course>>

    fun observeCourseDetail(courseId: Long): Flow<CourseDetail?>

    suspend fun refreshCourses(): AppResult<Unit>

    suspend fun setLessonCompleted(lessonId: Long, isCompleted: Boolean)
}
