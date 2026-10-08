package com.jassi.learningdashboard.domain.model

import kotlin.math.roundToInt

object ProgressCalculator {

    fun percent(completedLessons: Int, totalLessons: Int): Int {
        if (totalLessons <= 0) return 0
        val completed = completedLessons.coerceIn(0, totalLessons)
        return (completed * 100.0 / totalLessons).roundToInt()
    }
}
