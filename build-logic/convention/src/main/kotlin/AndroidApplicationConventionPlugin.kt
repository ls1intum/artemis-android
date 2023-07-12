import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

//Adapted from: https://github.com/android/nowinandroid/blob/3ca68d49eaeed8bb177d49c9c78249bb6bce3c5f/build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureInstanceSelectionFlavor(this)
                defaultConfig.targetSdk = 33
            }
        }
    }
}