package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun ImageVector.toPainter() = rememberVectorPainter(this)
