import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("maven-publish")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

group = "net.k1ra.hoodies_network_kmm"
version = System.getenv("releaseName") ?: "999999.999999.999999"

kotlin {
    android {
        namespace = "net.k1ra.hoodies_network_kmm"
        compileSdk {
            //noinspection WrongGradleMethod
            val (major, minor) = libs.versions.android.compileSdk.get().split(".").map { it.toInt() } + listOf(0)
            version = release(major) { minorApiLevel = minor }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources.enable = true

        withHostTest {
            enableCoverage = true
        }
        withDeviceTest {
            enableCoverage = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    val iosFrameworkName = "HoodiesNetworkKmm"

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework(iosFrameworkName)
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.coroutines)
            api(libs.kotlin.serialization)
            api(libs.kotlin.reflect)
            api(libs.kotlin.datetime)

            api(libs.korlibs.crypto)

            implementation(libs.androidx.room3.runtime)

            implementation(libs.compose.foundation)
            implementation(libs.k1ra.sharedpref)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlin.coroutines.test)
            implementation(libs.uuid)
            implementation(libs.compose.components.resources)
        }

        androidMain.dependencies {
            implementation(libs.androidx.crypto)

            implementation(libs.androidx.room3.sqlite)
        }

        iosMain.dependencies {
            implementation(libs.androidx.room3.sqlite)
        }

        jvmMain.dependencies {
            implementation(libs.androidx.room3.sqlite)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)

            implementation(libs.androidx.room3.sqlite.web)
            implementation(libs.androidx.sqlite.opfs)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    add("kspWasmJs", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

// Android host tests run on the desktop JVM, where the Android flavour of BundledSQLiteDriver
// cannot find libsqliteJni. Provide the host native library from the JVM artifact instead.
val sqliteHostNatives: Configuration by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
}

dependencies {
    sqliteHostNatives("androidx.sqlite:sqlite-bundled-jvm:${libs.versions.sqlite.get()}")
}

val sqliteHostNativesPlatform: String = run {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    val isArm = arch.contains("aarch64") || arch.contains("arm")
    when {
        os.contains("mac") -> "osx_arm64"
        os.contains("win") -> "windows_x64"
        isArm -> "linux_arm64"
        else -> "linux_x64"
    }
}

val extractSqliteHostNatives by tasks.registering(Sync::class) {
    from(sqliteHostNatives.elements.map { files -> files.map { zipTree(it) } }) {
        include("natives/$sqliteHostNativesPlatform/*")
        eachFile { path = name }
        includeEmptyDirs = false
    }
    into(layout.buildDirectory.dir("sqliteHostNatives"))
}

tasks.withType<Test>().matching { it.name.contains("AndroidHostTest", ignoreCase = true) }.configureEach {
    dependsOn(extractSqliteHostNatives)
    val nativesDir = extractSqliteHostNatives.map { it.destinationDir.absolutePath }
    doFirst {
        val existing = systemProperties["java.library.path"]?.toString()
            ?: System.getProperty("java.library.path")
        systemProperty(
            "java.library.path",
            listOfNotNull(nativesDir.get(), existing).joinToString(File.pathSeparator)
        )
    }
}

publishing {
    repositories {
        maven {
            name = "k1ra-nexus"
            url = uri("https://k1ra.net/nexus/repository/public/")

            credentials(PasswordCredentials::class) {
                username = System.getenv("NEXUS_USERNAME") ?: "anonymous"
                password = System.getenv("NEXUS_PASSWORD") ?: ""
            }
        }
    }
}

tasks{
    register<Jar>("dokkaJar") {
        from(dokkaGeneratePublicationHtml)
        dependsOn(dokkaGeneratePublicationHtml)
        archiveClassifier.set("javadoc")
    }
}