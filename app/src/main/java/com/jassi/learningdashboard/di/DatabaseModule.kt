package com.jassi.learningdashboard.di

import android.content.Context
import androidx.room.Room
import com.jassi.learningdashboard.data.local.LearningDatabase
import com.jassi.learningdashboard.data.local.dao.CourseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): LearningDatabase =
        Room.databaseBuilder(context, LearningDatabase::class.java, LearningDatabase.NAME)
            .build()

    @Provides
    fun courseDao(database: LearningDatabase): CourseDao = database.courseDao()
}
