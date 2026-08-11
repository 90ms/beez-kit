plugins { id("beezkit.android.library") }

android.namespace = "io.github.beez.beezkit.inspector.network"

dependencies {
    implementation(project(":toolkit:inspector:core"))
    testImplementation(libs.junit4)
}

