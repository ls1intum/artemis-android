package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings.CourseNotificationSettingsScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Serializable
private data class CourseNotificationSettingRoute(val courseId: Long)

fun NavController.navigateToCourseNotification(courseId: Long) {
    navigate(CourseNotificationSettingRoute(courseId))
}

fun NavGraphBuilder.courseNotificationScreen(
    navController: NavController,
) {
    animatedComposable<CourseNotificationSettingRoute> { backStackEntry ->
        val route: CourseNotificationSettingRoute = backStackEntry.toRoute()

        CourseNotificationSettingsScreen(
            viewModel =  koinViewModel { parametersOf(route.courseId) },
            onNavigateBack = navController::navigateUp,
        )
    }
}
