package com.jassi.learningdashboard.domain

import com.jassi.learningdashboard.domain.model.Course
import com.jassi.learningdashboard.domain.model.ProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun `no lessons is zero percent rather than a divide by zero`() {
        assertEquals(0, ProgressCalculator.percent(completedLessons = 0, totalLessons = 0))
        assertEquals(0, ProgressCalculator.percent(completedLessons = 3, totalLessons = 0))
    }

    @Test
    fun `exact fractions map to whole percentages`() {
        assertEquals(0, ProgressCalculator.percent(0, 20))
        assertEquals(65, ProgressCalculator.percent(13, 20))
        assertEquals(100, ProgressCalculator.percent(20, 20))
    }

    @Test
    fun `inexact fractions round half up`() {
        assertEquals(33, ProgressCalculator.percent(1, 3))
        assertEquals(67, ProgressCalculator.percent(2, 3))
        assertEquals(13, ProgressCalculator.percent(1, 8))
    }

    @Test
    fun `a single completed lesson never rounds down to zero percent`() {
        assertEquals(1, ProgressCalculator.percent(1, 200))
    }

    @Test
    fun `counts outside the valid range are clamped, not trusted`() {
        assertEquals(100, ProgressCalculator.percent(completedLessons = 25, totalLessons = 20))
        assertEquals(0, ProgressCalculator.percent(completedLessons = -5, totalLessons = 20))
    }

    @Test
    fun `course derives progress from cached lessons and ignores the reported value`() {
        val course = course(
            totalLessons = 20,
            completedLessons = 13,
            reportedProgressPercent = 99,
            hasCachedSyllabus = true,
        )

        assertEquals(65, course.progressPercent)
    }

    @Test
    fun `course falls back to the reported value until its syllabus is cached`() {
        val course = course(
            totalLessons = 20,
            completedLessons = 0,
            reportedProgressPercent = 40,
            hasCachedSyllabus = false,
        )

        assertEquals(40, course.progressPercent)
    }

    @Test
    fun `completion flags follow the derived progress`() {
        val finished = course(20, 20, 0, hasCachedSyllabus = true)
        val untouched = course(20, 0, 0, hasCachedSyllabus = true)

        assertEquals(true, finished.isComplete)
        assertEquals(false, finished.isNotStarted)
        assertEquals(false, untouched.isComplete)
        assertEquals(true, untouched.isNotStarted)
    }

    private fun course(
        totalLessons: Int,
        completedLessons: Int,
        reportedProgressPercent: Int,
        hasCachedSyllabus: Boolean,
    ) = Course(
        id = 1L,
        title = "Python Programming",
        instructor = "John Smith",
        totalLessons = totalLessons,
        completedLessons = completedLessons,
        reportedProgressPercent = reportedProgressPercent,
        hasCachedSyllabus = hasCachedSyllabus,
        lastSyncedAtMillis = 0L,
    )
}
