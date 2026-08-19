import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    jacoco
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

android {
    namespace = "com.racktrack"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.racktrack"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = providers.gradleProperty("racktrack.versionCode").get().toInt()
        versionName = providers.gradleProperty("racktrack.versionName").get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Epoch ms at configuration time (formatted in-app). Avoids `java.*` in this DSL
        // where `java` is shadowed by the Android/Java extension.
        buildConfigField("long", "BUILD_EPOCH_MS", "${System.currentTimeMillis()}L")
        buildConfigField(
            "String",
            "REPO_URL",
            "\"https://github.com/Asarox33/RackTrack\"",
        )
        val admobAppId =
            providers.gradleProperty("racktrack.admobAppId").orElse(
                "ca-app-pub-3940256099942544~3347511713",
            ).get()
        val admobInterstitialUnitId =
            providers.gradleProperty("racktrack.admobInterstitialUnitId").orElse(
                "ca-app-pub-3940256099942544/1033173712",
            ).get()
        buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$admobInterstitialUnitId\"")
        manifestPlaceholders["adMobAppId"] = admobAppId
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Play: R8 mapping is embedded in the AAB; native symbols are forced in via
            // embedReleaseNativeDebugSymbolsInBundle (deps often ship already-stripped .so).
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            // Play upload key when keystore.properties exists; otherwise debug for local sideload.
            signingConfig =
                if (keystorePropertiesFile.exists()) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // Keep whatever native debug info deps still carry so AGP can extract / we can
        // package a Play-compatible symbols zip into the AAB.
        jniLibs {
            keepDebugSymbols += "**/*.so"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    implementation(libs.billing.ktx)
    // Play Console: play-services-basement still pulls fragment:1.1.0 — force a current line.
    implementation(libs.androidx.fragment)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.org.json)
    testImplementation(libs.kotlinx.coroutines.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    extensions.configure(JacocoTaskExtension::class) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

/**
 * Domain-focused JaCoCo report (race / 14.1 engines). UI is mostly untested Compose.
 * HTML: app/build/reports/jacoco/domainCoverage/html/index.html
 * XML:  app/build/reports/jacoco/domainCoverage/domainCoverage.xml
 */
tasks.register<JacocoReport>("domainCoverage") {
    group = "verification"
    description = "Generate JaCoCo coverage report for domain sources"
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    // AGP 9 built-in Kotlin compiler output (not tmp/kotlin-classes).
    val kotlinClasses =
        layout.buildDirectory.dir(
            "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
        )
    classDirectories.setFrom(
        files(
            fileTree(kotlinClasses) {
                include("**/domain/**")
                exclude(
                    "**/*\$*",
                    "**/BuildConfig.*",
                    "**/R.class",
                    "**/R\$*.class",
                    "**/Manifest*.*",
                )
            },
        ),
    )
    sourceDirectories.setFrom(files("src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/*.exec")
        },
    )
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

/**
 * Play Console App bundle explorer expects native debug symbols. AGP only embeds them when
 * it can extract `.sym` / `.dbg` from unstripped libs (`mergeReleaseNativeDebugMetadata` is
 * often NO-SOURCE for pre-stripped AAR `.so`). Zip merged JNI libs and inject them into the
 * **unsigned** intermediary bundle so `signReleaseBundle` still produces a valid AAB.
 */
tasks.register("embedReleaseNativeDebugSymbolsInBundle") {
    group = "build"
    description =
        "Zip merged release .so libs and embed native-debug-symbols.zip in the intermediary AAB"
    val intermediaryAab =
        layout.buildDirectory.file(
            "intermediates/intermediary_bundle/release/packageReleaseBundle/intermediary-bundle.aab",
        )
    val nativeLibsDir =
        layout.buildDirectory.dir(
            "intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib",
        )
    val symbolsZip =
        layout.buildDirectory.file(
            "outputs/native-debug-symbols/release/native-debug-symbols.zip",
        )
    // Mutate the unsigned intermediary only — never the signed outputs AAB (breaks Play verify).
    dependsOn("packageReleaseBundle")
    inputs.dir(nativeLibsDir)
    outputs.file(symbolsZip)
    // packageReleaseBundle rewrites the intermediary without symbols; never skip inject
    // just because the symbols zip / .so tree look unchanged.
    outputs.upToDateWhen { false }

    doLast {
        val aab = intermediaryAab.get().asFile
        val libsRoot = nativeLibsDir.get().asFile
        check(aab.isFile) {
            "Missing $aab — packageReleaseBundle must run first"
        }
        check(libsRoot.isDirectory) {
            "Missing merged native libs at $libsRoot"
        }

        val zipOut = symbolsZip.get().asFile
        zipOut.parentFile.mkdirs()
        zipNativeAbiTree(libsRoot, zipOut)
        embedNativeDebugSymbolsZip(aab, zipOut)
        logger.lifecycle(
            "Embedded Play native debug symbols ({} bytes) into {}",
            zipOut.length(),
            aab.name,
        )
    }
}

tasks.configureEach {
    when (name) {
        "packageReleaseBundle" ->
            finalizedBy("embedReleaseNativeDebugSymbolsInBundle")
        "signReleaseBundle" ->
            dependsOn("embedReleaseNativeDebugSymbolsInBundle")
    }
}

private fun zipNativeAbiTree(libsRoot: File, zipOut: File) {
    ZipOutputStream(zipOut.outputStream().buffered()).use { zos ->
        libsRoot.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val entryName = file.relativeTo(libsRoot).invariantSeparatorsPath
                zos.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { input -> input.copyTo(zos) }
                zos.closeEntry()
            }
    }
}

private fun embedNativeDebugSymbolsZip(aab: File, symbolsZip: File) {
    val uri = URI.create("jar:" + aab.toURI())
    val env = mapOf("create" to "false")
    FileSystems.newFileSystem(uri, env).use { fs ->
        val dest =
            fs.getPath(
                "BUNDLE-METADATA/com.android.tools.build.debugsymbols/native-debug-symbols.zip",
            )
        Files.createDirectories(dest.parent)
        Files.copy(
            symbolsZip.toPath(),
            dest,
            StandardCopyOption.REPLACE_EXISTING,
        )
    }
}
