package de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service

import kotlinx.coroutines.flow.Flow

/**
 * Used for courses that do not have a code of conduct set. In these cases, we have to manage the acceptance locally.
 */
interface CodeOfConductStorageService {

    suspend fun isCodeOfConductAccepted(serverHost: String, courseId: Long, codeOfConduct: String): Flow<Boolean>

    suspend fun acceptCodeOfConduct(serverHost: String, courseId: Long, codeOfConduct: String)
}
