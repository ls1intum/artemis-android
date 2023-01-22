package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun Footer(
    modifier: Modifier,
    boxTwoWeight: Float = 1f / 3f,
    boxOne: @Composable BoxScope.() -> Unit,
    boxTwo: @Composable BoxScope.() -> Unit,
    boxThree: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .align(Alignment.Center)
        ) {
            val defaultModifier = Modifier
                .fillMaxHeight()

            val sideWeight = (1f - boxTwoWeight) / 2f

            Box(modifier = defaultModifier.weight(sideWeight)) {
                boxOne()
            }

            Box(modifier = defaultModifier.weight(boxTwoWeight)) {
                boxTwo()
            }

            Box(modifier = defaultModifier.weight(sideWeight)) {
                boxThree()
            }
        }
    }
}