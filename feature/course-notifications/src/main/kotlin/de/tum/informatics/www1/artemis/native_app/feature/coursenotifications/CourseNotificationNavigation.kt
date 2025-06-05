package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
private data class CourseNotificationScreenRoute(val courseId: Long)

fun NavController.navigateToCourseNotification(courseId: Long) {
    navigate(CourseNotificationScreenRoute(courseId))
}

fun NavGraphBuilder.courseNotificationScreen(
    navController: NavController,
) {
    animatedComposable<CourseNotificationScreenRoute> { backStackEntry ->
        val route: CourseNotificationScreenRoute = backStackEntry.toRoute()

        CourseNotificationScreen(
            courseId = route.courseId,
            onNavigateBack = navController::navigateUp
        )
    }
}
