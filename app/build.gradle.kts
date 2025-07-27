plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.gauravbajaj.interviewready"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gauravbajaj.interviewready"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":presentation"))

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    // Testing - Unit, Integration, and UI tests
    // Unit Testing
    testImplementation(libs.junit) // JUnit framework for unit testing
    testImplementation(libs.turbine) // Testing Flow emissions

    // Android Instrumentation Testing (UI & Integration)
    androidTestImplementation(libs.androidx.junit) // AndroidX Test Library for JUnit
    androidTestImplementation(libs.androidx.espresso.core) // Espresso for UI testing

    // Debugging and Testing Utilities for Compose
    // Note: Some of these might be duplicated from the Compose UI section, ensure only necessary ones are kept.
    // debugImplementation(libs.androidx.ui.tooling) // Already included above for Compose Previews
    debugImplementation(libs.androidx.ui.test.manifest) // Test manifest for Compose UI tests
    debugImplementation(libs.androidx.ui.test.junit4) // JUnit 4 rules for Compose UI tests
    // debugImplementation(libs.androidx.ui.test.manifest) // Duplicate, can be removed
}
