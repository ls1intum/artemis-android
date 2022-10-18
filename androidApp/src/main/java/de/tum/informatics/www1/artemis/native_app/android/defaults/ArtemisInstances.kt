package de.tum.informatics.www1.artemis.native_app.android.defaults

import androidx.annotation.StringRes
import de.tum.informatics.www1.artemis.native_app.android.BuildConfig
import de.tum.informatics.www1.artemis.native_app.android.R

object ArtemisInstances {

    val TUM_ARTEMIS = ArtemisInstance(
        serverUrl = "https://artemis.ase.in.tum.de/",
        name = R.string.artemis_instance_tum_production,
        type = ArtemisInstance.Type.PRODUCTION
    )

    val CODEABILITY = ArtemisInstance(
        serverUrl = "https://artemis.codeability.uibk.ac.at/",
        name = R.string.artemis_instance_codeability_production,
        type = ArtemisInstance.Type.PRODUCTION
    )

    val HOCHSCHULE_MÜNCHEN = ArtemisInstance(
        serverUrl = "https://artemis.cs.hm.edu/",
        name = R.string.artemis_instance_hm_production,
        type = ArtemisInstance.Type.PRODUCTION
    )

    val TUM_TEST_SERVER_0 = ArtemisInstance(
        serverUrl = "https://artemis-test0.artemis.in.tum.de/",
        name = R.string.artemis_instance_tum_test_server_0,
        type = ArtemisInstance.Type.TEST
    )

    val TUM_TEST_SERVER_1 = ArtemisInstance(
        serverUrl = "https://artemis-test1.artemis.in.tum.de/",
        name = R.string.artemis_instance_tum_test_server_1,
        type = ArtemisInstance.Type.TEST
    )

    val TUM_TEST_SERVER_2 = ArtemisInstance(
        serverUrl = "https://artemistest5.ase.in.tum.de/",
        name = R.string.artemis_instance_tum_test_server_2,
        type = ArtemisInstance.Type.TEST
    )

    val instances: List<ArtemisInstance> =
        (if (BuildConfig.DEBUG) {
            listOf(TUM_TEST_SERVER_0, TUM_TEST_SERVER_1, TUM_TEST_SERVER_2)
        } else emptyList()) + listOf(TUM_ARTEMIS, CODEABILITY, HOCHSCHULE_MÜNCHEN)

    data class ArtemisInstance(val serverUrl: String, @StringRes val name: Int, val type: Type) {
        enum class Type {
            PRODUCTION,
            TEST,
            CUSTOM
        }
    }
}