@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.lang.Boolean
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
    }

    object BuildConfigFields {
        const val HasInstanceRestriction = "hasInstanceRestriction"

        const val DefaultServerUrl = "defaultServerUrl"
    }
}

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = 34

        defaultConfig {
            minSdk = 30
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
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental",
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

    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
    }
}

internal fun Project.configureInstanceSelectionFlavor(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        flavorDimensions += ProductFlavors.Dimensions.InstanceSelection.Key

        productFlavors {
            if (!Boolean.getBoolean("skip.flavor.${ProductFlavors.Dimensions.InstanceSelection.Flavors.FreeInstanceSelection}")) {
                create(ProductFlavors.Dimensions.InstanceSelection.Flavors.FreeInstanceSelection) {
                    dimension = ProductFlavors.Dimensions.InstanceSelection.Key

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
            }

            if (!Boolean.getBoolean("skip.flavor.${ProductFlavors.Dimensions.InstanceSelection.Flavors.Tum}")) {
                create(ProductFlavors.Dimensions.InstanceSelection.Flavors.Tum) {
                    dimension = ProductFlavors.Dimensions.InstanceSelection.Key

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
}

fun CommonExtension<*, *, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}
