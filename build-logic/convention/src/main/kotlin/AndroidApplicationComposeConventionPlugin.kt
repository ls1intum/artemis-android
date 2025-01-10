import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

//Copy from: https://github.com/android/nowinandroid/blob/3ca68d49eaeed8bb177d49c9c78249bb6bce3c5f/build-logic/convention/src/main/kotlin/AndroidApplicationComposeConventionPlugin.kt

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val extension = extensions.getByType<ApplicationExtension>()

            configureCompose(extension)
        }
    }
}