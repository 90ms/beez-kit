plugins {
    id("beezkit.android.library")
    id("beezkit.android.compose")
}

android.namespace = "io.github.beez.beezkit.inspector.core"

dependencies {
    api(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    testImplementation(libs.junit4)
}

