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
            // binaryOption("bundleId", "com.mquniversity.tcct.iosApp")
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

        // pod("GoogleMaps") {
        //     version = libs.versions.podsGoogleMaps.get()
        //     extraOpts += listOf("-compiler-option", "-fmodules")
        // }

        // Optional properties
        // Configure the Pod name here instead of changing the Gradle project name
        // name = "MyCocoaPod"

        framework {
            // Required properties
            // Framework name configuration. Use this property instead of deprecated 'frameworkName'
            baseName = "shared"

            // Optional properties
            // Specify the framework linking type. It's dynamic by default.
            isStatic = false
            // Dependency export
            // export(project(":anotherKMMModule"))
            // Bitcode embedding
            // embedBitcode(BITCODE)
        }

        // Maps custom Xcode configuration to NativeBuildType
        // xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        // xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
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
