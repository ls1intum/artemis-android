plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("artemis.android.room")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis"

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

    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.kotlinx.datetime)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.views)
    implementation(libs.androidx.emoji2.views.helper)
    implementation(libs.androidx.emoji2.emojiPicker)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
}

tasks.register("fetchAndPrepareEmojis", emoji.FetchAndPrepareEmojisTask::class) {
    commit.set("d5676f0bb66c9c46b646db9b8a3d993b589bbe5c")
    set.set("14")
    outputDir.set(buildDir.resolve("generated/res/emoji/raw"))
}

project.afterEvaluate {
    tasks.getByName("preBuild").dependsOn(tasks.getByName("fetchAndPrepareEmojis"))
}