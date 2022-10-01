package de.tum.informatics.www1.artemis.native_app.android.defaults

import androidx.annotation.StringRes
import de.tum.informatics.www1.artemis.native_app.android.R

object ArtemisInstances {

    val TUM_ARTEMIS = ArtemisInstance(
        serverUrl = "https://artemis.ase.in.tum.de/",
        name = R.string.artemis_instance_tum_production,
        type = ArtemisInstance.Type.PRODUCTION
    )

    val TUM_TEST_SERVER_1 = ArtemisInstance(
        serverUrl = "https://artemis-test1.artemis.in.tum.de/",
        name = R.string.artemis_instance_tum_test_server_1,
        type = ArtemisInstance.Type.TEST
    )

    val TUM_TEST_SERVER_2 = ArtemisInstance(
        serverUrl = "https://artemis-test2.artemis.in.tum.de/",
        name = R.string.artemis_instance_tum_test_server_2,
        type = ArtemisInstance.Type.TEST
    )

    data class ArtemisInstance(val serverUrl: String, @StringRes val name: Int, val type: Type) {
        enum class Type {
            PRODUCTION,
            TEST
        }

        enum class LoginType {
            TUM_ONLINE,
            USER_DATABASE
        }
    }
}