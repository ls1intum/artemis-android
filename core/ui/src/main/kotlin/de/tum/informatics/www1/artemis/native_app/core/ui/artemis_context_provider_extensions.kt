package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider

@Composable
fun ArtemisContextProvider.collectAsState() = this.flow.collectAsState(ArtemisContext.Empty)