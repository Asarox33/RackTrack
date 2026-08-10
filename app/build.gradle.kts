plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    jacoco
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
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            // Installable CI / GitHub Release APKs until a Play Store keystore exists.
            signingConfig = signingConfigs.getByName("debug")
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
