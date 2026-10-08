package com.jassi.learningdashboard.domain.model

data class CourseDetail(
    val course: Course,
    val lessons: List<Lesson>,
)
