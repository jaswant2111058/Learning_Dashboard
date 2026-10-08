package com.jassi.learningdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jassi.learningdashboard.core.ui.theme.LearningDashboardTheme
import com.jassi.learningdashboard.domain.repository.AuthRepository
import com.jassi.learningdashboard.presentation.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startLoggedIn = authRepository.hasActiveSession()

        setContent {
            LearningDashboardTheme {
                AppNavHost(startLoggedIn = startLoggedIn)
            }
        }
    }
}
