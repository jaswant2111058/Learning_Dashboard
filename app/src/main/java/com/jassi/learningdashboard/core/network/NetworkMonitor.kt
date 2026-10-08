package com.jassi.learningdashboard.core.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    fun isOnline(): Boolean

    val isOnline: Flow<Boolean>
}
