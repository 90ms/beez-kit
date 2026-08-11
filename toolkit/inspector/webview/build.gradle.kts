plugins { id("beezkit.android.library") }

android.namespace = "io.github.beez.beezkit.inspector.webview"

dependencies {
    implementation(project(":toolkit:inspector:core"))
    testImplementation(libs.junit4)
}

