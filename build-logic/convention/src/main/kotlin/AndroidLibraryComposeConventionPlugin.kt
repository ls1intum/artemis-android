import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

//Adapted from: https://github.com/android/nowinandroid/blob/bbc5460b624d67b64b5b5118f8a0e1763427e7e4/build-logic/convention/src/main/kotlin/AndroidLibraryComposeConventionPlugin.kt
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val extension = extensions.getByType<LibraryExtension>()
            configureCompose(extension)
        }
    }
}