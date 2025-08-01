package de.tum.informatics.www1.artemis.native_app.android.ui

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.courseNotificationScreen
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.navigateToCourseNotification
import de.tum.informatics.www1.artemis.native_app.feature.courseregistration.courseRegistration
import de.tum.informatics.www1.artemis.native_app.feature.courseregistration.navigateToCourseRegistration
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.course
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.navigateToCourse
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.dashboard
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.navigateToDashboard
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseScreenRoute
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseViewDestination
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseViewMode
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.exercise
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.navigateToExercise
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.detail.faqDetail
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.detail.navigateToFaqDetail
import de.tum.informatics.www1.artemis.native_app.feature.force_update.ui.updateNavGraph
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.navigateToLecture
import de.tum.informatics.www1.artemis.native_app.feature.login.LoginScreenRoute
import de.tum.informatics.www1.artemis.native_app.feature.login.loginNavGraph
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.navigateToQuizParticipation
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.quizParticipation
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.navigateToQuizResult
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.quizResults
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.navigateToSettings
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.settingsNavGraph

fun NavGraphBuilder.rootNavGraph(
    navController: NavController,
    onDisplayThirdPartyLicenses: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onSkipUpdate: () -> Unit
) {
    val onNavigateToTextExerciseParticipation =
        { exerciseId: Long, participationId: Long ->
            navController.navigateToExercise(
                exerciseId = exerciseId,
                viewMode = ExerciseViewMode.TextParticipation(participationId)
            ) {}
        }

    val onNavigateToExerciseResultView = { exerciseId: Long ->
        navController.navigateToExercise(
            exerciseId = exerciseId,
            viewMode = ExerciseViewMode.ViewResult
        ) {}
    }

    val onParticipateInQuiz = { courseId: Long, exerciseId: Long, isPractice: Boolean ->
        navController.navigateToQuizParticipation(
            courseId,
            exerciseId,
            if (isPractice) QuizType.Practice else QuizType.Live
        )
    }

    val onClickViewQuizResults = { courseId: Long, exerciseId: Long ->
        navController.navigateToQuizResult(courseId, exerciseId)
    }


    loginNavGraph(
        onFinishedLoginFlow = { deepLink ->
            if (deepLink == null) {
                // Navigate to the course overview and remove the login screen from the navigation stack.
                navController.navigateToDashboard {
                    popUpTo<LoginScreenRoute> {
                        inclusive = true
                    }
                }
            } else {
                try {
                    navController.navigate(
                        Uri.parse(deepLink),
                        navOptions {
                            popUpTo<LoginScreenRoute>()
                        }
                    )
                } catch (_: IllegalArgumentException) {
                    navController.navigateToDashboard {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            }
        },
        onRequestOpenSettings = {
            navController.navigateToSettings { }
        }
    )

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
        onRegisteredInCourse = { courseId ->
            navController.navigateUp()
            navController.navigateToCourse(courseId) { }
        }
    )

    course(
        onNavigateToExercise = { exerciseId ->
            navController.navigateToExercise(
                exerciseId = exerciseId,
                showSideBarIcon = false,
                viewMode = ExerciseViewMode.Overview
            ) { }
        },
        onNavigateToExerciseResultView = onNavigateToExerciseResultView,
        onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
        onParticipateInQuiz = onParticipateInQuiz,
        onViewQuizResults = onClickViewQuizResults,
        onNavigateToLecture = { _, lectureId ->
            navController.navigateToLecture(
                lectureId = lectureId
            ) { }
        },
        onNavigateToFaq = { faqId ->
            navController.navigateToFaqDetail(faqId) { }
        },
        onNavigateBack = navController::navigateUp,
        onNavigateNotificationSection = { courseId ->
            navController.navigateToCourseNotification(courseId)
        }
    )

    exercise(
        onNavigateBack = navController::navigateUp,
        onParticipateInQuiz = onParticipateInQuiz,
        onClickViewQuizResults = onClickViewQuizResults
    )

    lecture(
        onViewExercise = { exerciseId ->
            navController.navigateToExercise(
                exerciseId,
                showSideBarIcon = false,
                ExerciseViewMode.Overview
            ) { }
        }
    )

    faqDetail()

    quizParticipation(
        onLeaveQuiz = {
            val previousBackStackEntry = navController.previousBackStackEntry
            if (previousBackStackEntry?.destination?.route == ExerciseScreenRoute::class.qualifiedName.orEmpty()) {
                previousBackStackEntry.savedStateHandle[ExerciseViewDestination.REQUIRE_RELOAD_KEY] =
                    true
            }
            navController.navigateUp()
        }
    )

    quizResults()

    settingsNavGraph(
        navController = navController,
        onDisplayThirdPartyLicenses = onDisplayThirdPartyLicenses
    )

    updateNavGraph(
        onOpenPlayStore = onOpenPlayStore,
        onSkipUpdate = onSkipUpdate
    )

    courseNotificationScreen(
        navController = navController
    )

}