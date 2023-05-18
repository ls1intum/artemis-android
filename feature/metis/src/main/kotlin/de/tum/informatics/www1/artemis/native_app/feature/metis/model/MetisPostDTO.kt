package de.tum.informatics.www1.artemis.native_app.feature.metis.model

import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import kotlinx.serialization.Serializable

@Serializable
data class MetisPostDTO(val post: StandalonePost, val action: MetisPostAction)