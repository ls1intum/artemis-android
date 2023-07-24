plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.quiz"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.flowlayout)

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
    testImplementation(project(":core:core-test"))
}

tasks.withType(Test::class) {
    android.sourceSets.getByName("main").res.srcDirs("src/test/res")
}
