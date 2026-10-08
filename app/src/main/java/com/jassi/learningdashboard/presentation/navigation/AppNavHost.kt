package com.jassi.learningdashboard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jassi.learningdashboard.presentation.dashboard.DashboardScreen
import com.jassi.learningdashboard.presentation.detail.CourseDetailScreen
import com.jassi.learningdashboard.presentation.login.LoginScreen

@Composable
fun AppNavHost(
    startLoggedIn: Boolean,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = if (startLoggedIn) DashboardRoute else LoginRoute,
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<DashboardRoute> {
            DashboardScreen(
                onCourseClick = { courseId -> navController.navigate(CourseDetailRoute(courseId)) },
                onLoggedOut = {
                    navController.navigate(LoginRoute) {
                        popUpTo<DashboardRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<CourseDetailRoute> { backStackEntry ->
            backStackEntry.toRoute<CourseDetailRoute>()
            CourseDetailScreen(onBack = navController::popBackStack)
        }
    }
}
