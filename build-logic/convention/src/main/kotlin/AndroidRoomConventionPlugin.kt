import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import util.libs

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("com.google.devtools.ksp")
            }

            dependencies {
                add("implementation", libs.findLibrary("androidx.room.runtime").get())
                add("annotationProcessor", libs.findLibrary("androidx.room.compiler").get())
                add("ksp", libs.findLibrary("androidx.room.compiler").get())
                add("implementation", libs.findLibrary("androidx.room.paging").get())
                add("implementation", libs.findLibrary("androidx.room.ktx").get())
            }
        }
    }
}
