package com.jassi.learningdashboard.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jassi.learningdashboard.core.network.NetworkMonitor
import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.core.result.DataError
import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.repository.AuthRepository
import com.jassi.learningdashboard.domain.usecase.ObserveCoursesUseCase
import com.jassi.learningdashboard.domain.usecase.RefreshCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeCourses: ObserveCoursesUseCase,
    private val refreshCourses: RefreshCoursesUseCase,
    private val authRepository: AuthRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private sealed interface SyncStatus {
        data object Idle : SyncStatus
        data object InFlight : SyncStatus
        data object Succeeded : SyncStatus
        data class Failed(val error: DataError) : SyncStatus
    }

    private val syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    val uiState: StateFlow<DashboardUiState> = combine(
        observeCourses(),
        syncStatus,
        networkMonitor.isOnline,
    ) { courses, status, isOnline ->
        toUiState(courses, status, isOnline)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DashboardUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        if (syncStatus.value == SyncStatus.InFlight) return

        syncStatus.value = SyncStatus.InFlight
        viewModelScope.launch {
            syncStatus.value = when (val result = refreshCourses()) {
                is AppResult.Success -> SyncStatus.Succeeded
                is AppResult.Failure -> SyncStatus.Failed(result.error)
            }
        }
    }

    fun onRefreshErrorDismissed() {
        if (syncStatus.value is SyncStatus.Failed) syncStatus.value = SyncStatus.Succeeded
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    private fun toUiState(
        courses: List<Course>,
        status: SyncStatus,
        isOnline: Boolean,
    ): DashboardUiState = when {
        courses.isEmpty() -> when (status) {
            SyncStatus.Idle, SyncStatus.InFlight -> DashboardUiState.Loading
            is SyncStatus.Failed -> DashboardUiState.Failure(status.error)
            SyncStatus.Succeeded -> DashboardUiState.Empty
        }

        else -> DashboardUiState.Content(
            courses = courses,
            isRefreshing = status == SyncStatus.InFlight,
            isOffline = !isOnline,
            refreshError = (status as? SyncStatus.Failed)?.error,
            lastSyncedAtMillis = courses.maxOfOrNull { it.lastSyncedAtMillis },
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
