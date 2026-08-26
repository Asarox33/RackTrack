plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    jacoco
}

kotlin {
    jvm()

    android {
        namespace = "com.racktrack.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            // Domain unit tests run on JVM with JUnit 5 (see jvmTest).
        }
        jvmTest.dependencies {
            implementation(project.dependencies.platform(libs.junit.bom))
            implementation(libs.junit.jupiter)
            runtimeOnly(libs.junit.platform.launcher)
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    extensions.configure(JacocoTaskExtension::class) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

/**
 * Domain-focused JaCoCo report (race / 14.1 engines) from `:shared` JVM tests.
 * HTML: shared/build/reports/jacoco/domainCoverage/html/index.html
 * XML:  shared/build/reports/jacoco/domainCoverage/domainCoverage.xml
 */
tasks.register<JacocoReport>("domainCoverage") {
    group = "verification"
    description = "Generate JaCoCo coverage report for shared domain sources"
    dependsOn("jvmTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val kotlinClasses =
        layout.buildDirectory.dir("classes/kotlin/jvm/main")
    classDirectories.setFrom(
        files(
            fileTree(kotlinClasses) {
                include("**/domain/**")
                exclude("**/*\$*")
            },
        ),
    )
    sourceDirectories.setFrom(files("src/commonMain/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/jvmTest.exec", "jacoco/*.exec")
        },
    )
}

ktlint {
    android.set(false)
    ignoreFailures.set(false)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    source.setFrom(
        "src/commonMain/kotlin",
        "src/jvmMain/kotlin",
        "src/androidMain/kotlin",
    )
}
