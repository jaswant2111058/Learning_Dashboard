package com.jassi.learningdashboard.domain.model

data class Course(
    val id: Long,
    val title: String,
    val instructor: String,
    val totalLessons: Int,
    val completedLessons: Int,
    val reportedProgressPercent: Int,
    val hasCachedSyllabus: Boolean,
    val lastSyncedAtMillis: Long,
) {
    val progressPercent: Int
        get() = if (hasCachedSyllabus) {
            ProgressCalculator.percent(completedLessons, totalLessons)
        } else {
            reportedProgressPercent.coerceIn(0, 100)
        }

    val isComplete: Boolean get() = hasCachedSyllabus && totalLessons > 0 && completedLessons >= totalLessons
    val isNotStarted: Boolean get() = completedLessons == 0
}
