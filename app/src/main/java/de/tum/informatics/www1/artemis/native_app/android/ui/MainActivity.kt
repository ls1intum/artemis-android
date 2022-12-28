package de.tum.informatics.www1.artemis.native_app.android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import de.tum.informatics.www1.artemis.native_app.android.BuildConfig
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.create_standalone_post.createStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post.ViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post.navigateToStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post.standalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.courseRegistration
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.navigateToCourseRegistration
import de.tum.informatics.www1.artemis.native_app.feature.course_view.course
import de.tum.informatics.www1.artemis.native_app.feature.course_view.navigateToCourse
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.DASHBOARD_DESTINATION
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.dashboard
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.navigateToDashboard
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewDestination
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exercise
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.navigateToExercise
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lecture
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.navigateToLecture
import de.tum.informatics.www1.artemis.native_app.feature.login.LOGIN_DESTINATION
import de.tum.informatics.www1.artemis.native_app.feature.login.loginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.navigateToLogin
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.navigateToQuizParticipation
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.quizParticipation
import de.tum.informatics.www1.artemis.native_app.feature.settings.navigateToSettings
import de.tum.informatics.www1.artemis.native_app.feature.settings.settingsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get

/**
 * Main and only activity used in the android app.
 * Navigation is handled by decompose and jetpack compose.
 */
class MainActivity : AppCompatActivity() {

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
                    // Navigate to the course overview and remove the login screen from the navigation stack.
                    navController.navigateToDashboard {
                        popUpTo(LOGIN_DESTINATION) {
                            inclusive = true
                        }
                    }
                }

                // Use jetpack compose navigation for the navigation logic.
                NavHost(navController = navController, startDestination = startDestination) {
                    loginScreen(onLoggedIn)

                    dashboard(
                        onOpenSettings = {
                            navController.navigateToSettings { }
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
                        onNavigateToLecture = { courseId, lectureId ->
                            navController.navigateToLecture(
                                courseId = courseId,
                                lectureId = lectureId
                            ) { }
                        },
                        onNavigateBack = navController::navigateUp,
                        navController = navController
                    )

                    exercise(
                        navController = navController,
                        onNavigateBack = navController::navigateUp,
                        onParticipateInQuiz = { courseId, exerciseId, isPractice ->
                            val quizType = if (isPractice) QuizType.PRACTICE else QuizType.LIVE

                            navController.navigateToQuizParticipation(
                                courseId,
                                exerciseId,
                                quizType
                            )
                        }
                    )

                    lecture(
                        navController = navController,
                        onNavigateBack = navController::navigateUp,
                        onRequestOpenLink = { url ->
                            CustomTabsIntent.Builder().build()
                                .launchUrl(this@MainActivity, Uri.parse(url))
                        },
                        onViewExercise = { exerciseId ->
                            navController.navigateToExercise(exerciseId) { }
                        }
                    )

                    standalonePostScreen(
                        onNavigateUp = navController::navigateUp
                    )

                    createStandalonePostScreen(
                        onNavigateUp = navController::navigateUp,
                        onCreatedPost = { clientSidePostId, metisContext ->
                            navController.navigateUp()
                            navController.navigateToStandalonePostScreen(
                                clientSidePostId,
                                metisContext,
                                ViewType.POST
                            ) {}
                        }
                    )

                    quizParticipation(
                        onLeaveQuiz = {
                            val previousBackStackEntry = navController.previousBackStackEntry
                            if (previousBackStackEntry?.destination?.route == ExerciseViewDestination.EXERCISE_VIEW_ROUTE) {
                                previousBackStackEntry.savedStateHandle.set(
                                    ExerciseViewDestination.REQUIRE_RELOAD_KEY,
                                    true
                                )
                            }
                            navController.navigateUp()
                        }
                    )

                    settingsScreen(
                        versionCode = BuildConfig.VERSION_CODE,
                        versionName = BuildConfig.VERSION_NAME,
                        onLoggedOut = {
                            navController.navigateToLogin {
                                popUpTo(DASHBOARD_DESTINATION) {
                                    inclusive = true
                                }
                            }
                        },
                        onDisplayThirdPartyLicenses = {
                            val intent =
                                Intent(this@MainActivity, OssLicensesMenuActivity::class.java)
                            startActivity(intent)
                        },
                        onNavigateUp = navController::navigateUp
                    )
                }
            }
        }
    }
}