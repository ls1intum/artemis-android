package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

fun NavGraphBuilder.courseNavGraphBuilderExtensions(
    route: String,
    deepLink: String? = null,
    content: @Composable (courseId: Long) -> Unit
) {
    composable(
        route = route,
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType; nullable = false }
        ),
        deepLinks = if (deepLink != null) listOf(
            navDeepLink { uriPattern = deepLink }
        ) else emptyList()
    ) { backStackEntry ->
        val courseId =
            backStackEntry.arguments?.getLong("courseId")
        checkNotNull(courseId)

        content(courseId)
    }
}

fun NavGraphBuilder.conversationNavGraphBuilderExtension(
    route: String,
    deepLink: String,
    content: @Composable (courseId: Long, conversationId: Long) -> Unit
) {
    composable(
        route = route,
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType; nullable = false },
            navArgument("conversationId") { type = NavType.LongType; nullable = false }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = deepLink
            }
        )
    ) { backStackEntry ->
        val courseId =
            backStackEntry.arguments?.getLong("courseId")

        val conversationId =
            backStackEntry.arguments?.getLong("conversationId")

        checkNotNull(courseId)
        checkNotNull(conversationId)

        content(courseId, conversationId)
    }
}