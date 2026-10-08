package com.jassi.learningdashboard.data.repository

import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.core.result.toDataError
import com.jassi.learningdashboard.core.time.TimeProvider
import com.jassi.learningdashboard.data.local.dao.CourseDao
import com.jassi.learningdashboard.data.mapper.toDomain
import com.jassi.learningdashboard.data.mapper.toEntity
import com.jassi.learningdashboard.data.mapper.toLessonEntities
import com.jassi.learningdashboard.data.remote.api.CourseApi
import com.jassi.learningdashboard.di.IoDispatcher
import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.model.CourseDetail
import com.jassi.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val courseApi: CourseApi,
    private val courseDao: CourseDao,
    private val timeProvider: TimeProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        courseDao.observeCoursesWithProgress()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun observeCourseDetail(courseId: Long): Flow<CourseDetail?> =
        combine(
            courseDao.observeCourseWithProgress(courseId),
            courseDao.observeLessons(courseId),
        ) { course, lessons ->
            course?.let { CourseDetail(it.toDomain(), lessons.map { lesson -> lesson.toDomain() }) }
        }.flowOn(ioDispatcher)

    override suspend fun refreshCourses(): AppResult<Unit> = withContext(ioDispatcher) {
        try {
            val response = courseApi.getCourses()
            val syncedAt = timeProvider.nowMillis()

            courseDao.syncCourses(
                courses = response.courses.map { it.toEntity(syncedAt) },
                lessons = response.courses.flatMap { it.toLessonEntities() },
            )
            AppResult.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            AppResult.Failure(throwable.toDataError())
        }
    }

    override suspend fun setLessonCompleted(lessonId: Long, isCompleted: Boolean) {
        withContext(ioDispatcher) {
            courseDao.setLessonCompleted(lessonId, isCompleted)
        }
    }
}
