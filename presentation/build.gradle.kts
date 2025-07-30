plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.gauravbajaj.interviewready"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))
    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // Navigation - Jetpack Navigation for Compose
    implementation(libs.navigationCompose) // Navigation components for Jetpack Compose
    implementation(libs.androidx.hilt.navigation.compose)

    // Asynchronous Programming - Coroutines
    implementation(libs.coroutinesCore) // Core Kotlin coroutines library
    implementation(libs.coroutinesAndroid) // Android-specific coroutine support


    // Core Android & Kotlin
    implementation(libs.androidx.core.ktx) // Kotlin extensions for Android framework
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle KTX for coroutines and LiveData
    implementation(libs.lifecycleRuntime) // Lifecycle runtime components
    implementation(libs.lifecycleViewModel) // ViewModel library for managing UI-related data
    implementation(libs.lifecycleViewModelCompose) // ViewModel integration with Jetpack Compose
    implementation(libs.androidx.activity.compose) // Activity integration with Jetpack Compose

    // Jetpack Compose - UI Toolkit
    implementation(platform(libs.androidx.compose.bom)) // Bill of Materials for Compose versions
    implementation(libs.androidx.ui) // Core Compose UI library
    implementation(libs.androidx.ui.graphics) // Graphics library for Compose
    implementation(libs.androidx.ui.tooling.preview) // Tooling for Compose previews in Android Studio
    debugImplementation(libs.androidx.ui.tooling) // Debug implementation for Compose tooling
    implementation(libs.androidx.material3) // Material Design 3 components for Compose
    implementation(libs.androidx.material.icons.extended.v143)// Extended Material Icons for Compose

    // Image Loading - Efficiently loading and displaying images
    implementation(libs.coil) // Image loading library for Android backed by Kotlin Coroutines
    implementation(libs.coilCompose) // Coil integration with Jetpack Compose

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // For testing, need access to FakeUserApi
    testImplementation(project(":data")) // Alternatively, we can create a common test utils module for fakes

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.moshi) // JSON library for Android and Java
    testImplementation(libs.moshiKotlin) // Kotlin support for Moshi

}