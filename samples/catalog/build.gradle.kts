plugins { id("beezkit.android.application") }

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(project(":toolkit:throttle"))
    implementation(project(":toolkit:stacktrace"))
    implementation(project(":toolkit:measure"))
    debugImplementation(project(":toolkit:inspector:core"))
    implementation(project(":components:toast"))
    implementation(project(":components:snackbar"))
    implementation(project(":components:tooltip"))
    implementation(project(":components:skeleton"))
}

