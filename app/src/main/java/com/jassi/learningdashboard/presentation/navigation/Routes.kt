package com.jassi.learningdashboard.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

@Serializable
data object DashboardRoute

@Serializable
data class CourseDetailRoute(val courseId: Long)
