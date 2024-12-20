package de.tum.informatics.www1.artemis.native_app.core.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlin.reflect.KType


const val navigationAnimationDuration = 300

inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline enterTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    EnterTransition?)? = { defaultEnterTransition },
    noinline exitTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    ExitTransition?)? = { defaultExitTransition },
    noinline popEnterTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    EnterTransition?)? = { defaultPopEnterTransition },
    noinline popExitTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    ExitTransition?)? = { defaultPopExitTransition },
    noinline sizeTransform:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    SizeTransform?)? =
        null,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    sizeTransform = sizeTransform,
    typeMap = typeMap,
    deepLinks = deepLinks,
    content = content
)

val defaultNavigateForwardTransition = defaultEnterTransition togetherWith defaultExitTransition
val defaultNavigateBackTransition = defaultPopEnterTransition togetherWith defaultPopExitTransition
val defaultNeutralTransition = defaultScaleIn() togetherWith defaultScaleOut()

val defaultEnterTransition
    get() = defaultSlideIn(AnimatedContentTransitionScope.SlideDirection.Left)

val defaultExitTransition
    get() = defaultSlideOut(AnimatedContentTransitionScope.SlideDirection.Left)

val defaultPopEnterTransition
    get() = defaultSlideIn(AnimatedContentTransitionScope.SlideDirection.Right)

val defaultPopExitTransition
    get() = defaultSlideOut(AnimatedContentTransitionScope.SlideDirection.Right)


fun defaultSlideIn(
    direction: AnimatedContentTransitionScope.SlideDirection
): EnterTransition = slideInHorizontally { width ->
    return@slideInHorizontally when(direction) {
        AnimatedContentTransitionScope.SlideDirection.Left -> width
        else -> - width
    }
} + fadeIn()

fun defaultSlideOut(
    direction: AnimatedContentTransitionScope.SlideDirection
): ExitTransition = slideOutHorizontally { width ->
    return@slideOutHorizontally when(direction) {
        AnimatedContentTransitionScope.SlideDirection.Left -> - width
        else -> width
    }
} + fadeOut()

fun defaultScaleIn(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.INWARDS,
    initialScale: Float = if (direction == ScaleTransitionDirection.OUTWARDS) 0.9f else 1.1f
): EnterTransition = scaleIn(
    animationSpec = tween(navigationAnimationDuration),
    initialScale = initialScale
) + fadeIn(animationSpec = tween(navigationAnimationDuration))


fun defaultScaleOut(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
    targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9f else 1.1f
): ExitTransition = scaleOut(
    animationSpec = tween(
        durationMillis = navigationAnimationDuration,
    ), targetScale = targetScale
) + fadeOut(tween())


enum class ScaleTransitionDirection {
    INWARDS,
    OUTWARDS
}
