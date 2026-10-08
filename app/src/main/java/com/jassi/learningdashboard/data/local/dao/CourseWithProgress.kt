package com.jassi.learningdashboard.data.local.dao

import androidx.room.Embedded
import com.jassi.learningdashboard.data.local.entity.CourseEntity

data class CourseWithProgress(
    @Embedded val course: CourseEntity,
    val cachedLessons: Int,
    val completedLessons: Int,
)
