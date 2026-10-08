package com.jassi.learningdashboard.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.core.result.DataError
import com.jassi.learningdashboard.data.local.LearningDatabase
import com.jassi.learningdashboard.data.remote.api.CourseApi
import com.jassi.learningdashboard.data.remote.dto.CourseDto
import com.jassi.learningdashboard.data.remote.dto.CoursesResponseDto
import com.jassi.learningdashboard.data.remote.dto.LessonDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CourseRepositoryImplTest {

    private lateinit var database: LearningDatabase
    private lateinit var api: FakeCourseApi
    private lateinit var repository: CourseRepositoryImpl

    private var now = 1_000L

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LearningDatabase::class.java,
        ).allowMainThreadQueries().build()

        api = FakeCourseApi()
        repository = CourseRepositoryImpl(
            courseApi = api,
            courseDao = database.courseDao(),
            timeProvider = { now },
            ioDispatcher = Dispatchers.Unconfined,
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `a successful refresh caches courses with derived progress`() = runTest {
        api.response = CoursesResponseDto(listOf(pythonCourse()))

        assertTrue(repository.refreshCourses() is AppResult.Success)

        val courses = repository.observeCourses().first()
        assertEquals(1, courses.size)
        with(courses.single()) {
            assertEquals("Python Programming", title)
            assertEquals("John Smith", instructor)
            assertEquals(4, totalLessons)
            assertEquals(2, completedLessons)
            assertEquals(50, progressPercent)
            assertEquals(now, lastSyncedAtMillis)
        }
    }

    @Test
    fun `cached courses are still served after the network starts failing`() = runTest {
        api.response = CoursesResponseDto(listOf(pythonCourse()))
        repository.refreshCourses()

        api.failWith = IOException("no connectivity")
        val refreshResult = repository.refreshCourses()

        assertEquals(AppResult.Failure(DataError.Network), refreshResult)
        val courses = repository.observeCourses().first()
        assertEquals(1, courses.size)
        assertEquals(50, courses.single().progressPercent)

        val detail = repository.observeCourseDetail(PYTHON_ID).first()
        assertEquals(4, detail?.lessons?.size)
    }

    @Test
    fun `marking a lesson complete updates progress without any network call`() = runTest {
        api.response = CoursesResponseDto(listOf(pythonCourse()))
        repository.refreshCourses()
        api.failWith = IOException("no connectivity")

        val pending = repository.observeCourseDetail(PYTHON_ID).first()!!
            .lessons.first { !it.isCompleted }

        repository.setLessonCompleted(pending.id, isCompleted = true)

        val detail = repository.observeCourseDetail(PYTHON_ID).first()!!
        assertEquals(3, detail.course.completedLessons)
        assertEquals(75, detail.course.progressPercent)
        assertTrue(detail.lessons.single { it.id == pending.id }.isCompleted)
        assertEquals(75, repository.observeCourses().first().single().progressPercent)
    }

    @Test
    fun `a local completion survives a refresh that still reports the lesson as pending`() =
        runTest {
            api.response = CoursesResponseDto(listOf(pythonCourse()))
            repository.refreshCourses()
            repository.setLessonCompleted(lessonId = 103L, isCompleted = true)

            api.response = CoursesResponseDto(
                listOf(
                    pythonCourse(
                        lessons = pythonLessons().map { lesson ->
                            if (lesson.id == 103L) lesson.copy(title = "Functions (revised)") else lesson
                        },
                    ),
                ),
            )
            assertTrue(repository.refreshCourses() is AppResult.Success)

            val lessons = repository.observeCourseDetail(PYTHON_ID).first()!!.lessons
            val lesson = lessons.single { it.id == 103L }

            assertTrue(lesson.isCompleted)
            assertEquals("Functions (revised)", lesson.title)
            assertEquals(75, repository.observeCourses().first().single().progressPercent)
        }

    @Test
    fun `courses dropped from the payload are pruned along with their lessons`() = runTest {
        api.response = CoursesResponseDto(listOf(pythonCourse(), genAiCourse()))
        repository.refreshCourses()
        assertEquals(2, repository.observeCourses().first().size)

        api.response = CoursesResponseDto(listOf(pythonCourse()))
        repository.refreshCourses()

        assertEquals(listOf(PYTHON_ID), repository.observeCourses().first().map { it.id })
        assertNull(repository.observeCourseDetail(GEN_AI_ID).first())
    }

    @Test
    fun `an unreadable payload is reported as a serialization failure, not a network one`() =
        runTest {
            api.failWith = kotlinx.serialization.SerializationException("missing field")

            assertEquals(
                AppResult.Failure(DataError.Serialization),
                repository.refreshCourses(),
            )
        }

    private fun pythonLessons() = listOf(
        LessonDto(id = 101L, title = "Introduction", completed = true),
        LessonDto(id = 102L, title = "Variables & Data Types", completed = true),
        LessonDto(id = 103L, title = "Functions", completed = false),
        LessonDto(id = 104L, title = "OOP", completed = false),
    )

    private fun pythonCourse(lessons: List<LessonDto> = pythonLessons()) = CourseDto(
        id = PYTHON_ID,
        title = "Python Programming",
        instructor = "John Smith",
        progress = 65,
        lessons = lessons.size,
        syllabus = lessons,
    )

    private fun genAiCourse() = CourseDto(
        id = GEN_AI_ID,
        title = "Generative AI",
        instructor = "Sarah Williams",
        progress = 40,
        lessons = 2,
        syllabus = listOf(
            LessonDto(id = 201L, title = "What is Generative AI", completed = false),
            LessonDto(id = 202L, title = "Tokens & Embeddings", completed = false),
        ),
    )

    private class FakeCourseApi : CourseApi {
        var response: CoursesResponseDto = CoursesResponseDto()
        var failWith: Throwable? = null

        override suspend fun getCourses(): CoursesResponseDto {
            failWith?.let { throw it }
            return response
        }
    }

    private companion object {
        const val PYTHON_ID = 1L
        const val GEN_AI_ID = 2L
    }
}
