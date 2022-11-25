package de.tum.informatics.www1.artemis.native_app.core.communication

import de.tum.informatics.www1.artemis.native_app.core.model.metis.BasePost
import kotlinx.serialization.Serializable

@Serializable
data class MetisPostDTO(val post: BasePost, val action: MetisPostAction)