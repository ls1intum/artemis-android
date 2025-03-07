plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("artemis.android.room")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.conversation"

    sourceSets {
        named("main") {
            res.srcDir(buildDir.resolve("generated/res/emoji"))
        }
    }
}

dependencies {
    implementation(project(":core:device"))
    implementation(project(":feature:faq"))
    testImplementation(project(":core:ui-test"))

    implementation(project(":feature:metis:shared"))
    testImplementation(project(":feature:metis-test"))

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.placeholder.material)

    implementation(libs.koin.androidx.workmanager)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.paging.common)
    
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
}

tasks.register("fetchAndPrepareEmojis", emoji.FetchAndPrepareEmojisTask::class) {
    commit.set("d5676f0bb66c9c46b646db9b8a3d993b589bbe5c")
    set.set("14")
    outputDir.set(buildDir.resolve("generated/res/emoji/raw"))
}

project.afterEvaluate {
    tasks.getByName("preBuild").dependsOn(tasks.getByName("fetchAndPrepareEmojis"))
}