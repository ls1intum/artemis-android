@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import util.libs
import java.lang.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.apply

//Adapted from: https://github.com/android/nowinandroid/blob/bbc5460b624d67b64b5b5118f8a0e1763427e7e4/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/KotlinAndroid.kt

object ProductFlavors {
    object Dimensions {
        object InstanceSelection {
            const val Key = "instance-selection"

            object Flavors {
                const val FreeInstanceSelection = "unrestricted"

                const val Tum = "tum"
            }
        }

        object ReleaseType {
            const val Key = "release-type"

            object Flavors {
                const val Beta = "beta"

                const val Production = "production"
            }
        }
    }

    object BuildConfigFields {
        const val HasInstanceRestriction = "hasInstanceRestriction"

        const val DefaultServerUrl = "defaultServerUrl"

        const val IsBeta = "isBeta"
    }
}

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = libs.findVersion("compileSdk").get().toString().toInt()
        buildToolsVersion = libs.findVersion("buildToolsVersion").get().toString()

        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().toString().toInt()
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
            isCoreLibraryDesugaringEnabled = true
        }

        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview"
            )

            jvmTarget = JavaVersion.VERSION_17.toString()
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }

        // As of now, we can skip the linter on release builds.
        lint {
            checkReleaseBuilds = false
            checkTestSources = false
        }

        extensions.getByType(AndroidComponentsExtension::class).apply {
            if (Boolean.getBoolean("skip.debugVariants")) {
                beforeVariants(selector().withBuildType("debug")) { variantBuilder ->
                    variantBuilder.enable = false
                }
            }
        }
    }

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
    }

    configureReleaseTypeFlavors(commonExtension)
}

internal fun Project.configureReleaseTypeFlavors(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        flavorDimensions += ProductFlavors.Dimensions.ReleaseType.Key

        productFlavors {
            createFlavor(
                ProductFlavors.Dimensions.ReleaseType.Key,
                ProductFlavors.Dimensions.ReleaseType.Flavors.Beta
            ) {
                buildConfigField("boolean", ProductFlavors.BuildConfigFields.IsBeta, "true")
            }

            createFlavor(
                ProductFlavors.Dimensions.ReleaseType.Key,
                ProductFlavors.Dimensions.ReleaseType.Flavors.Production
            ) {
                buildConfigField("boolean", ProductFlavors.BuildConfigFields.IsBeta, "false")
            }
        }
    }
}

internal fun Project.configureInstanceSelectionFlavors(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        flavorDimensions += ProductFlavors.Dimensions.InstanceSelection.Key

        productFlavors {
            createFlavor(
                ProductFlavors.Dimensions.InstanceSelection.Key,
                ProductFlavors.Dimensions.InstanceSelection.Flavors.FreeInstanceSelection
            ) {
                buildConfigField(
                    "boolean",
                    ProductFlavors.BuildConfigFields.HasInstanceRestriction,
                    "false"
                )
                buildConfigField(
                    "String",
                    ProductFlavors.BuildConfigFields.DefaultServerUrl,
                    "\"\""
                )
            }

            createFlavor(
                ProductFlavors.Dimensions.InstanceSelection.Key,
                ProductFlavors.Dimensions.InstanceSelection.Flavors.Tum
            ) {
                buildConfigField(
                    "boolean",
                    ProductFlavors.BuildConfigFields.HasInstanceRestriction,
                    "true"
                )
                buildConfigField(
                    "String",
                    ProductFlavors.BuildConfigFields.DefaultServerUrl,
                    "\"https://artemis.cit.tum.de\""
                )
            }
        }
    }
}

private fun NamedDomainObjectContainer<out ProductFlavor>.createFlavor(
    dimensionKey: String,
    name: String,
    configure: ProductFlavor.() -> Unit
) {
    if (!Boolean.getBoolean("skip.flavor.$name")) {
        create(name) {
            dimension = dimensionKey

            configure()
        }
    }
}

fun CommonExtension<*, *, *, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}
