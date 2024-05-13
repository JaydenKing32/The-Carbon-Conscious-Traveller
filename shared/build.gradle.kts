plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.native.cocoapods)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.kotlinJvmTarget.get()
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
        }
        androidMain.dependencies {
            implementation(libs.android.driver)
            implementation(libs.koin.androidx.compose)
            // implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.play.services.maps)
            // implementation(libs.google.maps.services)
            implementation(libs.play.services.location)
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
}

android {
    namespace = "com.mquniversity.tcct.shared"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.mquniversity.tcct.shared.cache")
        }
    }
}
