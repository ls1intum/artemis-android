package de.tum.informatics.www1.artemis.native_app.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith


object DefaultTransition {
    const val duration = 300

    val fadeIn = fadeIn(tween(duration))
    val fadeOut = fadeOut(tween(duration))

    val navigateForward = enter togetherWith exit
    val navigateBack = popEnter togetherWith popExit
    val navigateNeutral = fadeIn togetherWith fadeOut

    val enter
        get() = slideIn(AnimatedContentTransitionScope.SlideDirection.Left)

    val exit
        get() = slideOut(AnimatedContentTransitionScope.SlideDirection.Left)

    val popEnter
        get() = slideIn(AnimatedContentTransitionScope.SlideDirection.Right)

    val popExit
        get() = slideOut(AnimatedContentTransitionScope.SlideDirection.Right)

    fun slideIn(
        direction: AnimatedContentTransitionScope.SlideDirection
    ): EnterTransition = slideInHorizontally(
        animationSpec = tween(duration)
    ) { width ->
        return@slideInHorizontally when(direction) {
            AnimatedContentTransitionScope.SlideDirection.Left -> width
            else -> - width
        }
    } + fadeIn

    fun slideOut(
        direction: AnimatedContentTransitionScope.SlideDirection
    ): ExitTransition = slideOutHorizontally(
        animationSpec = tween(duration)
    ) { width ->
        return@slideOutHorizontally when(direction) {
            AnimatedContentTransitionScope.SlideDirection.Left -> - width
            else -> width
        }
    } + fadeOut
}
