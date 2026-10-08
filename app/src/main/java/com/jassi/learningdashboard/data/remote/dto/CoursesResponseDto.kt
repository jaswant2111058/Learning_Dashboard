package com.jassi.learningdashboard.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoursesResponseDto(
    val courses: List<CourseDto> = emptyList(),
)

@Serializable
data class CourseDto(
    val id: Long,
    val title: String = "",
    val instructor: String = "",
    val progress: Int = 0,
    val lessons: Int = 0,
    val syllabus: List<LessonDto> = emptyList(),
)

@Serializable
data class LessonDto(
    val id: Long,
    val title: String = "",
    val completed: Boolean = false,
)
