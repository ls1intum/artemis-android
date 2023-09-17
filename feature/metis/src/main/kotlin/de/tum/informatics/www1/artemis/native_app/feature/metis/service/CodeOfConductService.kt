package de.tum.informatics.www1.artemis.native_app.feature.metis.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse

interface CodeOfConductService {

    suspend fun getIsCodeOfConductAccepted(courseId: Long, serverUrl: String, authToken: String): NetworkResponse<Boolean>

    suspend fun acceptCodeOfConduct(courseId: Long, serverUrl: String, authToken: String): NetworkResponse<Unit>
}
