package com.jassi.learningdashboard.fake

import com.jassi.learningdashboard.core.network.NetworkMonitor
import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.model.CourseDetail
import com.jassi.learningdashboard.domain.repository.AuthRepository
import com.jassi.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCourseRepository : CourseRepository {

    val courses = MutableStateFlow<List<Course>>(emptyList())
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)

    var refreshGate: CompletableDeferred<Unit>? = null

    var refreshCount = 0
        private set

    private val lessons = MutableStateFlow<Map<Long, CourseDetail>>(emptyMap())

    override fun observeCourses(): Flow<List<Course>> = courses

    override fun observeCourseDetail(courseId: Long): Flow<CourseDetail?> =
        lessons.map { it[courseId] }

    override suspend fun refreshCourses(): AppResult<Unit> {
        refreshCount++
        refreshGate?.await()
        return refreshResult
    }

    override suspend fun setLessonCompleted(lessonId: Long, isCompleted: Boolean) {
        lessons.value = lessons.value.mapValues { (_, detail) ->
            val updated = detail.lessons.map { lesson ->
                if (lesson.id == lessonId) lesson.copy(isCompleted = isCompleted) else lesson
            }
            detail.copy(
                lessons = updated,
                course = detail.course.copy(completedLessons = updated.count { it.isCompleted }),
            )
        }
    }

    fun setDetail(detail: CourseDetail) {
        lessons.value = lessons.value + (detail.course.id to detail)
    }
}

class FakeNetworkMonitor(initiallyOnline: Boolean = true) : NetworkMonitor {
    private val online = MutableStateFlow(initiallyOnline)

    override fun isOnline(): Boolean = online.value
    override val isOnline: Flow<Boolean> = online

    fun setOnline(value: Boolean) {
        online.value = value
    }
}

class FakeAuthRepository(private var loggedIn: Boolean = false) : AuthRepository {
    var loginAttempts = 0
        private set
    var loginResult: AppResult<Unit> = AppResult.Success(Unit)
    var logoutCount = 0
        private set

    override fun hasActiveSession(): Boolean = loggedIn

    override suspend fun login(email: String, password: String): AppResult<Unit> {
        loginAttempts++
        return loginResult.also { if (it is AppResult.Success) loggedIn = true }
    }

    override suspend fun logout() {
        logoutCount++
        loggedIn = false
    }
}
