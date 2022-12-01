import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("artemis.android.library")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.communication"

    sourceSets {
        named("main") {
            res.srcDir(buildDir.resolve("generated/res/emoji"))
        }
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:websocket"))
    implementation(project(":core:datastore"))
    implementation(project(":core:device"))

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.kotlinx.datetime)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.views)
    implementation(libs.androidx.emoji2.views.helper)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.appcompat)
}

tasks.register("fetchAndPrepareEmojis", emoji.FetchAndPrepareEmojisTask::class) {
    commit.set("d5676f0bb66c9c46b646db9b8a3d993b589bbe5c")
    set.set("14")
}

tasks.withType(KotlinCompile::class).forEach { kotlinCompile ->
    project.afterEvaluate {
        kotlinCompile.dependsOn(tasks.getByName("fetchAndPrepareEmojis"))
    }
}