import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val sharedProjectName = "com.mquniversity.tcct.shared"

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.native.cocoapods)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidTarget {
        compilations.all {
            tasks.withType<KotlinJvmCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "shared"
            linkerOpts.add("-lsqlite3")
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        commonMain.dependencies {
            // implementation(libs.runtime)
            // implementation(libs.primitive.adapters)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)
            // implementation(libs.coroutines.extensions)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material)
            implementation(compose.components.resources)
            api(libs.kmm.viewmodel.core)
            implementation(libs.compose.navigation)
        }
        androidMain.dependencies {
            implementation(libs.android.driver)
            implementation(libs.koin.androidx.compose)
            // implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.play.services.maps)
            // implementation(libs.google.maps.services)
            implementation(libs.play.services.location)
            implementation(libs.google.maps.compose)
            implementation(libs.google.maps.compose.utils)
        }
        iosMain.dependencies {
            implementation(libs.native.driver)
        }
    }

    cocoapods {
        // Required properties
        // Specify the required Pod version here. Otherwise, the Gradle project version is used.
        // version = "1.0"
        summary = "The Carbon-Conscious Traveller"
        source = "https://github.com/CocoaPods/Specs.git"
        homepage = "https://github.com/JaydenKing32/The-Carbon-Conscious-Traveller"
        license = "MIT"
        ios.deploymentTarget = "15.0"
        podfile = project.file("../iosApp/Podfile")

        pod("GoogleMaps") {
            version = libs.versions.podsGoogleMaps.get()
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        framework {
            baseName = "shared"
            isStatic = true
        }
    }

    task("testClasses")
}

buildkonfig {
    packageName = sharedProjectName

    defaultConfigs {
        val googleMapsApiKey = gradleLocalProperties(rootDir, providers).getProperty("MAPS_API_KEY")
        buildConfigField(FieldSpec.Type.STRING, "googleMapsApiKey", googleMapsApiKey)
    }
}

android {
    namespace = sharedProjectName
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("${sharedProjectName}.cache")
        }
    }
}
