pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val pathsToTry = listOf(
                "$rootDir/../org.thepalaceproject.android.platform/build_libraries.toml",
                "$rootDir/../ekirjasto-android-platform/build_libraries.toml",
                "$rootDir/org.thepalaceproject.android.platform/build_libraries.toml",
                "$rootDir/ekirjasto-android-platform/build_libraries.toml",
            )
            for (pathToTry in pathsToTry) {
                if (file(pathToTry).exists()) {
                    println("Using build_libraries.toml from path: $pathToTry")
                    from(files(pathToTry))
                    break
                }
            }
        }
    }

    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "org.thepalaceproject.theme"

include(":core")
include(":sandbox")
