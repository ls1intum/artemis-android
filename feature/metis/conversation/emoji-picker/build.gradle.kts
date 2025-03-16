plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker"

    sourceSets {
        named("main") {
            res.srcDir(buildDir.resolve("generated/res/emoji"))
        }
    }
}

dependencies {

    implementation(libs.androidx.dataStore.preferences)
}

tasks.register("fetchAndPrepareEmojis", emoji.FetchAndPrepareEmojisTask::class) {
    commit.set("d5676f0bb66c9c46b646db9b8a3d993b589bbe5c")
    set.set("14")
    outputDir.set(buildDir.resolve("generated/res/emoji/raw"))
}

project.afterEvaluate {
    tasks.getByName("preBuild").dependsOn(tasks.getByName("fetchAndPrepareEmojis"))
}