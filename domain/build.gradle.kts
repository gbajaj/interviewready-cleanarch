plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // For testing, need access to FakeUserApi
    testImplementation(project(":data")) // Alternatively, we can create a common test utils module for fakes

    // Testing
    testImplementation(libs.moshi) // JSON library for Android and Java
    testImplementation(libs.moshiKotlin) // Kotlin support for Moshi
    testImplementation(testFixtures(project(":data")))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test.v173)
    testImplementation(libs.mockk.v1134)

}