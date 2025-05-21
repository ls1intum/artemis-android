plugins {
    id("artemis.android.library")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
    id("artemis.android.flavor.library.instanceSelection")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.ui"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    api(project(":core:data"))
    api(project(":core:websocket"))
    api(project(":core:datastore"))

    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.windowsizeclass)
    debugApi(libs.androidx.compose.ui.tooling)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.navigation.compose)
    api(libs.coil2.base)
    api(libs.coil.compose)
    api(libs.coil.network)
    api(libs.accompanist.webview)
    api(libs.noties.markwon.core)

    implementation(libs.coil.compose.core)
    implementation(libs.coil.test)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.browser)

    implementation(libs.noties.markwon.ext.strikethrough)
    implementation(libs.noties.markwon.ext.tables)
    implementation(libs.noties.markwon.html)
    implementation(libs.noties.markwon.simple.ext)
    implementation(libs.noties.markwon.linkify)
    implementation(libs.noties.markwon.image.coil)

    debugImplementation(libs.edge2edge.preview)

    testImplementation(project(":core:common-test"))
}