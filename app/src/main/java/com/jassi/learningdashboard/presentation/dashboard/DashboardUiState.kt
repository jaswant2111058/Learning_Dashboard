package com.jassi.learningdashboard.presentation.dashboard

import com.jassi.learningdashboard.core.result.DataError
import com.jassi.learningdashboard.domain.model.Course

sealed interface DashboardUiState {

    data object Loading : DashboardUiState

    data object Empty : DashboardUiState

    data class Failure(val error: DataError) : DashboardUiState

    data class Content(
        val courses: List<Course>,
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false,
        val refreshError: DataError? = null,
        val lastSyncedAtMillis: Long? = null,
    ) : DashboardUiState
}
