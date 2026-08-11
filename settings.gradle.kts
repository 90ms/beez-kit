pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "beez-kit"

include(
    ":toolkit:throttle",
    ":toolkit:stacktrace",
    ":toolkit:measure",
    ":toolkit:inspector:core",
    ":toolkit:inspector:network",
    ":toolkit:inspector:event",
    ":toolkit:inspector:webview",
    ":components:toast",
    ":components:snackbar",
    ":components:tooltip",
    ":components:skeleton",
    ":samples:catalog",
)

