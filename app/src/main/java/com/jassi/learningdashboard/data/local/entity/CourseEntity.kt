package com.jassi.learningdashboard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val instructor: String,
    val totalLessons: Int,
    val reportedProgressPercent: Int,
    val syncedAtMillis: Long,
)
