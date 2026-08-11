plugins {
    id("beezkit.android.library")
    id("beezkit.android.compose")
}

android.namespace = "io.github.beez.beezkit.throttle"

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    implementation(libs.compose.foundation)
    testImplementation(libs.junit4)
}

