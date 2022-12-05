package de.tum.informatics.www1.artemis.native_app.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.standalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.courseRegistration
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.navigateToCourseRegistration
import de.tum.informatics.www1.artemis.native_app.feature.course_view.course
import de.tum.informatics.www1.artemis.native_app.feature.course_view.navigateToCourse
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.DASHBOARD_DESTINATION
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.dashboard
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.navigateToDashboard
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exercise
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.navigateToExercise
import de.tum.informatics.www1.artemis.native_app.feature.login.LOGIN_DESTINATION
import de.tum.informatics.www1.artemis.native_app.feature.login.loginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.navigateToLogin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Main and only activity used in the android app.
 * Navigation is handled by decompose and jetpack compose.
 */
class MainActivity : ComponentActivity() {

    private val accountService: AccountService = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //When the user is logged in, immediately display the course overview.
        val startDestination = runBlocking {
            when (accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> DASHBOARD_DESTINATION
                AccountService.AuthenticationData.NotLoggedIn -> LOGIN_DESTINATION
            }
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()

                val onLoggedIn = {
                    //Navigate to the course overview and remove the login screen from the navigation stack.
                    navController.navigateToDashboard {
                        popUpTo(LOGIN_DESTINATION) {
                            inclusive = true
                        }
                    }
                }

                //Use jetpack compose navigation for the navigation logic.
                NavHost(navController = navController, startDestination = startDestination) {
                    loginScreen(onLoggedIn)

                    dashboard(
                        onLogout = {
                            //Navigate to the login screen and remove the course overview screen from the navigation stack.
                            navController.navigateToLogin {
                                popUpTo(DASHBOARD_DESTINATION) {
                                    inclusive = true
                                }
                            }
                        },
                        onClickRegisterForCourse = {
                            navController.navigateToCourseRegistration { }
                        },
                        onViewCourse = { courseId ->
                            navController.navigateToCourse(courseId) { }
                        }
                    )

                    courseRegistration(
                        onNavigateUp = navController::navigateUp,
                        onRegisteredInCourse = { courseId ->
                            navController.navigateUp()
                            navController.navigateToCourse(courseId) { }
                        }
                    )

                    course(
                        onNavigateToExercise = { exerciseId ->
                            navController.navigateToExercise(exerciseId) { }
                        },
                        onNavigateBack = navController::navigateUp,
                        navController = navController
                    )

                    exercise(
                        onNavigateBack = navController::navigateUp
                    )

                    standalonePostScreen(
                        onNavigateUp = navController::navigateUp
                    )
                }
            }
        }
    }
}