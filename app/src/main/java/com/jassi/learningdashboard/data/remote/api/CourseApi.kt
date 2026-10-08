package com.jassi.learningdashboard.data.remote.api

import com.jassi.learningdashboard.data.remote.dto.CoursesResponseDto
import retrofit2.http.GET

interface CourseApi {

    @GET("courses.json")
    suspend fun getCourses(): CoursesResponseDto
}
