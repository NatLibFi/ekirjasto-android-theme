import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Verify
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

val gradleVersionRequired = "8.2.1"
val gradleVersionReceived = gradle.gradleVersion

if (gradleVersionRequired != gradleVersionReceived) {
    throw GradleException(
        "Gradle version $gradleVersionRequired is required to run this build. You are using Gradle $gradleVersionReceived",
    )
}

plugins {
    signing

    id("org.jetbrains.kotlin.jvm")
        .version("1.9.0")
        .apply(false)

    id("org.jetbrains.kotlin.android")
        .version("1.9.0")
        .apply(false)

    /*
     * The AndroidX plugin for navigation (including view binding generation).
     *
     * https://developer.android.com/jetpack/androidx/releases/navigation
     */

    id("androidx.navigation.safeargs.kotlin")
        .version("2.7.1")
        .apply(false)

    id("com.android.library")
        .version("8.1.0")
        .apply(false)

    id("com.android.application")
        .version("8.1.0")
        .apply(false)

    /*
     * Android Junit5 plugin. Required to run JUnit 5 tests on Android projects.
     *
     * https://github.com/mannodermaus/android-junit5
     */

    id("de.mannodermaus.android-junit5")
        .version("1.9.3.0")
        .apply(false)

    /*
     * Download plugin. Used to fetch artifacts such as Scando during the build.
     *
     * https://plugins.gradle.org/plugin/de.undercouch.download
     */

    id("de.undercouch.download")
        .version("5.4.0")
        .apply(false)

    /*
     * https://developers.google.com/android/guides/google-services-plugin
     */

    id("com.google.gms.google-services")
        .version("4.3.15")
        .apply(false)

    /*
     * https://firebase.google.com/docs/crashlytics/get-started?platform=android
     */

    id("com.google.firebase.crashlytics")
        .version("2.9.9")
        .apply(false)

//    id("maven-publish")
}

/*
 * The various paths used during the build.
 */

val palaceRootBuildDirectory =
    "$rootDir/build"
val palaceDeployDirectory =
    "$palaceRootBuildDirectory/maven"
val palaceScandoJarFile =
    "$rootDir/scando.jar"
val palaceKtlintJarFile =
    "$rootDir/ktlint.jar"

/**
 * Convenience functions to read strongly-typed values from property files.
 */

fun property(
    project: Project,
    name: String,
): String {
    return project.extra[name] as String
}

fun propertyOptional(project: Project, name: String): String? {
    val map = project.extra
    if (map.has(name)) {
        return map[name] as String?
    }
    return null
}

fun propertyInt(
    project: Project,
    name: String,
): Int {
    val text = property(project, name)
    return text.toInt()
}

fun propertyBoolean(
    project: Project,
    name: String,
): Boolean {
    val text = property(project, name)
    return text.toBooleanStrict()
}

fun propertyBooleanOptional(
    project: Project,
    name: String,
    defaultValue: Boolean,
): Boolean {
    val value = propertyOptional(project, name) ?: return defaultValue
    return value.toBooleanStrict()
}

/*
 * A task that cleans up the Maven deployment directory. The "clean" tasks of
 * each project are configured to depend upon this task. This prevents any
 * deployment of stale artifacts to remote repositories.
 */

//val cleanTask = task("CleanMavenDeployDirectory", Delete::class) {
//    this.delete.add(palaceDeployDirectory)
//}

/**
 * A function to download and verify the ktlint jar file.
 *
 * @return The verification task
 */

fun createKtlintDownloadTask(project: Project): Task {
    val ktlintVersion =
        "0.50.0"
    val ktlintSHA256 =
        "c704fbc28305bb472511a1e98a7e0b014aa13378a571b716bbcf9d99d59a5092"
    val ktlintSource =
        "https://repo1.maven.org/maven2/com/pinterest/ktlint/$ktlintVersion/ktlint-$ktlintVersion-all.jar"

    val ktlintMakeDirectory =
        project.task("KtlintMakeDirectory") {
            mkdir(palaceRootBuildDirectory)
        }

    val ktlintDownload =
        project.task("KtlintDownload", Download::class) {
            src(ktlintSource)
            dest(file(palaceKtlintJarFile))
            overwrite(true)
            onlyIfModified(true)
            this.dependsOn.add(ktlintMakeDirectory)
        }

    return project.task("KtlintDownloadVerify", Verify::class) {
        src(file(palaceKtlintJarFile))
        checksum(ktlintSHA256)
        algorithm("SHA-256")
        this.dependsOn(ktlintDownload)
    }
}

/**
 * A task to execute ktlint to check sources.
 */

val ktlintPatterns: List<String> = arrayListOf(
    "*/src/**/*.kt",
    "*/build.gradle.kts",
    "build.gradle.kts",
    "!*/src/test/**",
)

fun createKtlintCheckTask(project: Project): Task {
    val commandLineArguments: ArrayList<String> = arrayListOf(
        "java",
        "-jar",
        palaceKtlintJarFile,
    )
    commandLineArguments.addAll(ktlintPatterns)

    return project.task("KtlintCheck", Exec::class) {
        commandLine = commandLineArguments
    }
}

/**
 * A task to execute ktlint to reformat sources.
 */

fun createKtlintFormatTask(project: Project): Task {
    val commandLineArguments: ArrayList<String> = arrayListOf(
        "java",
        "-jar",
        palaceKtlintJarFile,
        "-F",
    )
    commandLineArguments.addAll(ktlintPatterns)

    return project.task("KtlintFormat", Exec::class) {
        commandLine = commandLineArguments
    }
}

/*
 * Create a task in the root project that downloads ktlint.
 */

lateinit var ktlintDownloadTask: Task

allprojects {

    /*
     * Configure the project metadata.
     */

    this.group =
        property(this, "GROUP")
    this.version =
        property(this, "VERSION_NAME")

    val jdkBuild =
        propertyInt(this, "org.thepalaceproject.build.jdkBuild")
    val jdkBytecodeTarget =
        propertyInt(this, "org.thepalaceproject.build.jdkBytecodeTarget")

    /*
     * Configure builds and tests for various project types.
     */

    when (extra["POM_PACKAGING"]) {
        "pom" -> {
            logger.info("Configuring ${this.project} $version as a pom project")
        }

        "apk" -> {
            logger.info("Configuring ${this.project} $version as an apk project")

            apply(plugin = "com.android.application")
            apply(plugin = "org.jetbrains.kotlin.android")

            /*
             * Configure the JVM toolchain version that we want to use for Kotlin.
             */

            val kotlin: KotlinAndroidProjectExtension =
                this.extensions["kotlin"] as KotlinAndroidProjectExtension
            val java: JavaPluginExtension =
                this.extensions["java"] as JavaPluginExtension

            kotlin.jvmToolchain(jdkBuild)
            java.toolchain.languageVersion.set(JavaLanguageVersion.of(jdkBuild))

            /*
             * Configure the various required Android properties.
             */

            val android: ApplicationExtension =
                this.extensions["android"] as ApplicationExtension

            android.namespace =
                property(this, "POM_ARTIFACT_ID")
            android.compileSdk =
                propertyInt(this, "org.thepalaceproject.build.androidSDKCompile")

            android.defaultConfig {
                multiDexEnabled = true
                targetSdk =
                    propertyInt(this@allprojects, "org.thepalaceproject.build.androidSDKTarget")
                minSdk =
                    propertyInt(this@allprojects, "org.thepalaceproject.build.androidSDKMinimum")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            /*
             * Produce JDK bytecode of the correct version.
             */

            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions.jvmTarget = jdkBytecodeTarget.toString()
            }
            java.sourceCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)
            java.targetCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)

            android.compileOptions {
                encoding = "UTF-8"
                sourceCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)
                targetCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)
            }
        }

        "aar" -> {
            logger.info("Configuring ${this.project} $version as an aar project")

            apply(plugin = "com.android.library")
            apply(plugin = "org.jetbrains.kotlin.android")
            apply(plugin = "de.mannodermaus.android-junit5")

            /*
             * Configure the JVM toolchain version that we want to use for Kotlin.
             */

            val kotlin: KotlinAndroidProjectExtension =
                this.extensions["kotlin"] as KotlinAndroidProjectExtension
            val java: JavaPluginExtension =
                this.extensions["java"] as JavaPluginExtension

            kotlin.jvmToolchain(jdkBuild)
            java.toolchain.languageVersion.set(JavaLanguageVersion.of(jdkBuild))

            /*
             * Configure the various required Android properties.
             */

            val android: LibraryExtension =
                this.extensions["android"] as LibraryExtension

            android.namespace =
                property(this, "POM_ARTIFACT_ID")
            android.compileSdk =
                propertyInt(this, "org.thepalaceproject.build.androidSDKCompile")

            android.defaultConfig {
                multiDexEnabled = true
                minSdk =
                    propertyInt(this@allprojects, "org.thepalaceproject.build.androidSDKMinimum")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            /*
             * Produce JDK bytecode of the correct version.
             */

            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions.jvmTarget = jdkBytecodeTarget.toString()
            }
            java.sourceCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)
            java.targetCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)

            android.compileOptions {
                encoding = "UTF-8"
                sourceCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)
                targetCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)
            }

            android.testOptions {
                execution = "ANDROIDX_TEST_ORCHESTRATOR"
                animationsDisabled = true

                /*
                 * Enable the production of reports for all unit tests.
                 */

                unitTests {
                    isIncludeAndroidResources = true

                    all { test ->
                        // Required for the Mockito ByteBuddy agent on modern VMs.
                        test.systemProperty("jdk.attach.allowAttachSelf", "true")
                        test.reports.html.required = true
                        test.reports.junitXml.required = true
                    }
                }
            }

        }

        "jar" -> {
            logger.info("Configuring ${this.project} $version as a jar project")

            apply(plugin = "java-library")
            apply(plugin = "org.jetbrains.kotlin.jvm")

            /*
             * Configure the JVM toolchain versions that we want to use for Kotlin and Java.
             */

            val kotlin: KotlinProjectExtension =
                this.extensions["kotlin"] as KotlinProjectExtension
            val java: JavaPluginExtension =
                this.extensions["java"] as JavaPluginExtension

            kotlin.jvmToolchain(jdkBuild)
            java.toolchain.languageVersion.set(JavaLanguageVersion.of(jdkBuild))

            /*
             * Produce JDK bytecode of the correct version.
             */

            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions.jvmTarget = jdkBytecodeTarget.toString()
            }
            java.sourceCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)
            java.targetCompatibility = JavaVersion.toVersion(jdkBytecodeTarget)

            /*
             * Configure JUnit tests.
             */

            tasks.named<Test>("test") {
                useJUnitPlatform()

                // Required for the Mockito ByteBuddy agent on modern VMs.
                systemProperty("jdk.attach.allowAttachSelf", "true")

                testLogging {
                    events("passed")
                }

                this.reports.html.required = true
                this.reports.junitXml.required = true
            }
        }
    }

    /*
     * Configure some aggressive version resolution behaviour. The listed configurations have
     * transitive dependency resolution enabled; all other configurations do not. This forces
     * projects to be extremely explicit about what is imported.
     */

    val transitiveConfigurations = setOf(
        "androidTestDebugImplementation",
        "androidTestDebugImplementationDependenciesMetadata",
        "androidTestImplementation",
        "androidTestImplementationDependenciesMetadata",
        "androidTestReleaseImplementation",
        "androidTestReleaseImplementationDependenciesMetadata",
        "annotationProcessor",
        "debugAndroidTestCompilationImplementation",
        "debugAndroidTestImplementation",
        "debugAndroidTestImplementationDependenciesMetadata",
        "debugAnnotationProcessor",
        "debugAnnotationProcessorClasspath",
        "debugUnitTestCompilationImplementation",
        "debugUnitTestImplementation",
        "debugUnitTestImplementationDependenciesMetadata",
        "kotlinBuildToolsApiClasspath",
        "kotlinCompilerClasspath",
        "kotlinCompilerPluginClasspath",
        "kotlinCompilerPluginClasspathDebug",
        "kotlinCompilerPluginClasspathDebugAndroidTest",
        "kotlinCompilerPluginClasspathDebugUnitTest",
        "kotlinCompilerPluginClasspathMain",
        "kotlinCompilerPluginClasspathRelease",
        "kotlinCompilerPluginClasspathReleaseUnitTest",
        "kotlinCompilerPluginClasspathTest",
        "kotlinKlibCommonizerClasspath",
        "kotlinNativeCompilerPluginClasspath",
        "kotlinScriptDef",
        "kotlinScriptDefExtensions",
        "mainSourceElements",
        "releaseAnnotationProcessor",
        "releaseAnnotationProcessorClasspath",
        "releaseUnitTestCompilationImplementation",
        "releaseUnitTestImplementation",
        "releaseUnitTestImplementationDependenciesMetadata",
        "testDebugImplementation",
        "testDebugImplementationDependenciesMetadata",
        "testFixturesDebugImplementation",
        "testFixturesDebugImplementationDependenciesMetadata",
        "testFixturesImplementation",
        "testFixturesImplementationDependenciesMetadata",
        "testFixturesReleaseImplementation",
        "testFixturesReleaseImplementationDependenciesMetadata",
        "testImplementation",
        "testImplementationDependenciesMetadata",
        "testReleaseImplementation",
        "testReleaseImplementationDependenciesMetadata",
    )

    /*
     * Write the set of available configurations to files, for debugging purposes. Plugins can
     * add new configurations at any time, and so it's nice to have a list of the available
     * configurations visible.
     */

    val configurationsActual = mutableSetOf<String>()
    configurations.all {
        configurationsActual.add(this.name)
    }
    File("configurations.txt").writeText(configurationsActual.joinToString("\n"))

    configurations.all {
        isTransitive = transitiveConfigurations.contains(name)
        // resolutionStrategy.failOnVersionConflict()
    }

}
