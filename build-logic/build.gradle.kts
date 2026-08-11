plugins {
    `kotlin-dsl`
}

group = "io.github.beez.beezkit.buildlogic"

dependencies {
    implementation("com.android.tools.build:gradle:9.2.1")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.3.10")
}

kotlin {
    jvmToolchain(17)
}

