plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "io.github.beez.beezkit.samples.catalog"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.beez.beezkit.samples.catalog"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures.compose = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

