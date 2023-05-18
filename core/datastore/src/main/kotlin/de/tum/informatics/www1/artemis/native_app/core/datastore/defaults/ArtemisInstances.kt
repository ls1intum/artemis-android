package de.tum.informatics.www1.artemis.native_app.core.datastore.defaults

import androidx.annotation.StringRes
import de.tum.informatics.www1.artemis.native_app.core.datastore.BuildConfig
import de.tum.informatics.www1.artemis.native_app.core.datastore.R

object ArtemisInstances {

    val TumArtemis = ArtemisInstance(
        serverUrl = "https://artemis.ase.in.tum.de/",
        name = R.string.artemis_instance_tum_production,
        type = ArtemisInstance.Type.PRODUCTION
    )

    private val Codeability = ArtemisInstance(
        serverUrl = "https://artemis.codeability.uibk.ac.at/",
        name = R.string.artemis_instance_codeability_production,
        type = ArtemisInstance.Type.PRODUCTION
    )

    private val Kit = ArtemisInstance(
        serverUrl = "https://artemis.praktomat.cs.kit.edu/",
        name = R.string.artemis_instance_kiot_production,
        type = ArtemisInstance.Type.PRODUCTION
    )

    private val TumTs0 = ArtemisInstance(
        serverUrl = "https://artemis-test0.artemis.in.tum.de/",
        name = R.string.artemis_instance_tum_test_server_0,
        type = ArtemisInstance.Type.TEST
    )

    private val TumTs1 = ArtemisInstance(
        serverUrl = "https://artemis-test1.artemis.in.tum.de/",
        name = R.string.artemis_instance_tum_test_server_1,
        type = ArtemisInstance.Type.TEST
    )

    private val TumTs2 = ArtemisInstance(
        serverUrl = "https://artemistest5.ase.in.tum.de/",
        name = R.string.artemis_instance_tum_test_server_2,
        type = ArtemisInstance.Type.TEST
    )

    val instances: List<ArtemisInstance> =
        (if (BuildConfig.DEBUG) {
            listOf(TumTs0, TumTs1, TumTs2)
        } else emptyList()) + listOf(TumArtemis, Kit, Codeability)

    data class ArtemisInstance(val serverUrl: String, @StringRes val name: Int, val type: Type) {
        enum class Type {
            PRODUCTION,
            TEST,
            CUSTOM
        }
    }
}