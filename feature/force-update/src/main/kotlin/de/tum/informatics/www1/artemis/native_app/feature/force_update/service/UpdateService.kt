package de.tum.informatics.www1.artemis.native_app.feature.force_update.service


import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import kotlin.random.Random

interface UpdateService {
    suspend fun getLatestVersion(): NetworkResponse<Int>
}

class UpdateServiceImpl : UpdateService {
    override suspend fun getLatestVersion(): NetworkResponse<Int> {

        return NetworkResponse.Response(Random.nextInt(615, 626))
    }
}
