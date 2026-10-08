package com.jassi.learningdashboard.data.mapper

import com.jassi.learningdashboard.data.local.dao.CourseWithProgress
import com.jassi.learningdashboard.data.local.entity.CourseEntity
import com.jassi.learningdashboard.data.local.entity.LessonEntity
import com.jassi.learningdashboard.data.remote.dto.CourseDto
import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.model.Lesson

fun CourseDto.toEntity(syncedAtMillis: Long) = CourseEntity(
    id = id,
    title = title,
    instructor = instructor,
    totalLessons = if (syllabus.isNotEmpty()) syllabus.size else lessons.coerceAtLeast(0),
    reportedProgressPercent = progress.coerceIn(0, 100),
    syncedAtMillis = syncedAtMillis,
)

fun CourseDto.toLessonEntities(): List<LessonEntity> = syllabus.mapIndexed { index, lesson ->
    LessonEntity(
        id = lesson.id,
        courseId = id,
        title = lesson.title,
        position = index,
        isCompleted = lesson.completed,
    )
}

fun CourseWithProgress.toDomain() = Course(
    id = course.id,
    title = course.title,
    instructor = course.instructor,
    totalLessons = course.totalLessons,
    completedLessons = completedLessons,
    reportedProgressPercent = course.reportedProgressPercent,
    hasCachedSyllabus = cachedLessons > 0,
    lastSyncedAtMillis = course.syncedAtMillis,
)

fun LessonEntity.toDomain() = Lesson(
    id = id,
    courseId = courseId,
    title = title,
    position = position,
    isCompleted = isCompleted,
)
