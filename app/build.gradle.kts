configurations.all {
    resolutionStrategy {
        force("androidx.navigationevent:navigationevent-android:1.0.0-alpha01")
        force("androidx.navigationevent:navigationevent-compose-android:1.0.0-alpha01")
        force("androidx.activity:activity:1.10.0")
        force("androidx.activity:activity-ktx:1.10.0")
        force("androidx.activity:activity-compose:1.10.0")
        force("androidx.core:core-ktx:1.15.0")
    }
    exclude(group = "com.intellij", module = "annotations")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.creative.diagnostics"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.creative.diagnostics"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(projects.coreModel)
    implementation(projects.coreData)
    implementation(projects.coreSystem)
    implementation(projects.featureBattery)
    implementation(projects.featureNetwork)
    implementation(projects.featureThermal)
    implementation(projects.featureAutomation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}