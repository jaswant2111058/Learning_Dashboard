package com.jassi.learningdashboard.domain.model

data class Lesson(
    val id: Long,
    val courseId: Long,
    val title: String,
    val position: Int,
    val isCompleted: Boolean,
)
