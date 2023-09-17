package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import kotlinx.serialization.Serializable

@Serializable
data class MetisPostDTO(val post: StandalonePost, val action: MetisPostAction)