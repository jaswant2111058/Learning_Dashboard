package com.jassi.learningdashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.jassi.learningdashboard.data.local.entity.CourseEntity
import com.jassi.learningdashboard.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query(
        """
        SELECT c.*,
               (SELECT COUNT(*) FROM lessons l WHERE l.courseId = c.id) AS cachedLessons,
               (SELECT COUNT(*) FROM lessons l WHERE l.courseId = c.id AND l.isCompleted = 1)
                   AS completedLessons
        FROM courses c
        """,
    )
    fun observeCoursesWithProgress(): Flow<List<CourseWithProgress>>

    @Query(
        """
        SELECT c.*,
               (SELECT COUNT(*) FROM lessons l WHERE l.courseId = c.id) AS cachedLessons,
               (SELECT COUNT(*) FROM lessons l WHERE l.courseId = c.id AND l.isCompleted = 1)
                   AS completedLessons
        FROM courses c
        WHERE c.id = :courseId
        """,
    )
    fun observeCourseWithProgress(courseId: Long): Flow<CourseWithProgress?>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position ASC")
    fun observeLessons(courseId: Long): Flow<List<LessonEntity>>

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :lessonId")
    suspend fun setLessonCompleted(lessonId: Long, isCompleted: Boolean): Int

    @Upsert
    suspend fun upsertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewLessons(lessons: List<LessonEntity>): List<Long>

    @Query("UPDATE lessons SET title = :title, position = :position WHERE id = :id")
    suspend fun updateLessonContent(id: Long, title: String, position: Int)

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()

    @Query("DELETE FROM courses WHERE id NOT IN (:keepIds)")
    suspend fun deleteCoursesNotIn(keepIds: List<Long>)

    @Query("DELETE FROM lessons WHERE courseId = :courseId AND id NOT IN (:keepIds)")
    suspend fun deleteLessonsNotIn(courseId: Long, keepIds: List<Long>)

    @Transaction
    suspend fun syncCourses(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        if (courses.isEmpty()) {
            deleteAllCourses()
            return
        }

        upsertCourses(courses)
        deleteCoursesNotIn(courses.map { it.id })

        val insertedRowIds = insertNewLessons(lessons)
        lessons.forEachIndexed { index, lesson ->
            val alreadyExisted = insertedRowIds.getOrNull(index) == -1L
            if (alreadyExisted) {
                updateLessonContent(lesson.id, lesson.title, lesson.position)
            }
        }

        val lessonIdsByCourse = lessons.groupBy({ it.courseId }, { it.id })
        courses.forEach { course ->
            val keepIds = lessonIdsByCourse[course.id].orEmpty()
            if (keepIds.isNotEmpty()) {
                deleteLessonsNotIn(course.id, keepIds)
            }
        }
    }
}
