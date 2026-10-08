package com.jassi.learningdashboard.presentation

import app.cash.turbine.test
import com.jassi.learningdashboard.MainDispatcherRule
import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.core.result.DataError
import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.usecase.ObserveCoursesUseCase
import com.jassi.learningdashboard.domain.usecase.RefreshCoursesUseCase
import com.jassi.learningdashboard.fake.FakeAuthRepository
import com.jassi.learningdashboard.fake.FakeCourseRepository
import com.jassi.learningdashboard.fake.FakeNetworkMonitor
import com.jassi.learningdashboard.presentation.dashboard.DashboardUiState
import com.jassi.learningdashboard.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val courseRepository = FakeCourseRepository()
    private val authRepository = FakeAuthRepository(loggedIn = true)
    private val networkMonitor = FakeNetworkMonitor()

    private fun viewModel() = DashboardViewModel(
        observeCourses = ObserveCoursesUseCase(courseRepository),
        refreshCourses = RefreshCoursesUseCase(courseRepository),
        authRepository = authRepository,
        networkMonitor = networkMonitor,
    )

    @Test
    fun `stays in Loading while the first sync is in flight and the cache is empty`() = runTest {
        courseRepository.refreshGate = CompletableDeferred()

        viewModel().uiState.test {
            advanceUntilIdle()
            assertEquals(DashboardUiState.Loading, expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows cached courses with progress derived from lesson counts`() = runTest {
        courseRepository.courses.value = listOf(course(id = 1, completed = 13, total = 20))

        viewModel().uiState.test {
            advanceUntilIdle()

            val content = expectMostRecentItem() as DashboardUiState.Content
            assertEquals(1, content.courses.size)
            assertEquals(65, content.courses.single().progressPercent)
            assertFalse(content.isRefreshing)
            assertFalse(content.isOffline)
            assertNull(content.refreshError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a successful but empty sync is Empty, not an error`() = runTest {
        courseRepository.refreshResult = AppResult.Success(Unit)

        viewModel().uiState.test {
            advanceUntilIdle()
            assertEquals(DashboardUiState.Empty, expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed sync with an empty cache is a fatal error`() = runTest {
        courseRepository.refreshResult = AppResult.Failure(DataError.Network)

        viewModel().uiState.test {
            advanceUntilIdle()
            assertEquals(DashboardUiState.Failure(DataError.Network), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed sync with cached courses keeps the content and reports the error beside it`() =
        runTest {
            courseRepository.courses.value = listOf(course(id = 1, completed = 5, total = 10))
            courseRepository.refreshResult = AppResult.Failure(DataError.Network)
            networkMonitor.setOnline(false)

            viewModel().uiState.test {
                advanceUntilIdle()

                val content = expectMostRecentItem() as DashboardUiState.Content
                assertEquals(1, content.courses.size)
                assertEquals(DataError.Network, content.refreshError)
                assertTrue(content.isOffline)
                assertFalse(content.isRefreshing)
                assertEquals(1_000L, content.lastSyncedAtMillis)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `content is marked refreshing while a sync is in flight`() = runTest {
        courseRepository.courses.value = listOf(course(id = 1, completed = 5, total = 10))
        courseRepository.refreshGate = CompletableDeferred()

        viewModel().uiState.test {
            advanceUntilIdle()

            val content = expectMostRecentItem() as DashboardUiState.Content
            assertTrue(content.isRefreshing)
            assertEquals(1, content.courses.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `courses are ordered in progress first, then not started, then complete`() = runTest {
        courseRepository.courses.value = listOf(
            course(id = 1, completed = 10, total = 10, title = "Finished"),
            course(id = 2, completed = 0, total = 10, title = "Not started"),
            course(id = 3, completed = 2, total = 10, title = "Barely started"),
            course(id = 4, completed = 8, total = 10, title = "Nearly done"),
        )

        viewModel().uiState.test {
            advanceUntilIdle()

            val content = expectMostRecentItem() as DashboardUiState.Content
            assertEquals(
                listOf("Nearly done", "Barely started", "Not started", "Finished"),
                content.courses.map { it.title },
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a refresh is not started again while one is already in flight`() = runTest {
        courseRepository.refreshGate = CompletableDeferred()
        val viewModel = viewModel()

        advanceUntilIdle()
        viewModel.refresh()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(1, courseRepository.refreshCount)
    }

    @Test
    fun `dismissing a refresh error clears it without hiding the content`() = runTest {
        courseRepository.courses.value = listOf(course(id = 1, completed = 5, total = 10))
        courseRepository.refreshResult = AppResult.Failure(DataError.Network)
        val viewModel = viewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(
                DataError.Network,
                (expectMostRecentItem() as DashboardUiState.Content).refreshError,
            )

            viewModel.onRefreshErrorDismissed()
            advanceUntilIdle()

            val content = expectMostRecentItem() as DashboardUiState.Content
            assertNull(content.refreshError)
            assertEquals(1, content.courses.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout clears the session`() = runTest {
        viewModel().logout()
        advanceUntilIdle()

        assertEquals(1, authRepository.logoutCount)
    }

    private fun course(
        id: Long,
        completed: Int,
        total: Int,
        title: String = "Course $id",
    ) = Course(
        id = id,
        title = title,
        instructor = "Instructor",
        totalLessons = total,
        completedLessons = completed,
        reportedProgressPercent = 0,
        hasCachedSyllabus = true,
        lastSyncedAtMillis = 1_000L,
    )
}
