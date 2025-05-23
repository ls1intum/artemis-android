import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import util.libs

//Adapted from: https://github.com/android/nowinandroid/blob/bbc5460b624d67b64b5b5118f8a0e1763427e7e4/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/AndroidCompose.kt

internal fun Project.configureCompose(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
                "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
                "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            )
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("testImplementation", platform(bom))
            add("implementation", libs.findLibrary("koin.core").get())
            add("implementation", libs.findLibrary("koin.android").get())
            add("implementation", libs.findLibrary("koin.androidx.compose").get())
            add("implementation", libs.findLibrary("koin.android.compat").get())
            add("testImplementation", "androidx.compose.ui:ui-test-junit4")
            add("testImplementation", "androidx.compose.ui:ui-test-manifest")
        }
    }
}
