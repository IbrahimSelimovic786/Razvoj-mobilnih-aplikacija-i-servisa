plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services") // Plugin za Firebase
}

android {
    namespace = "com.example.familyhustle"
    compileSdk = 35
    viewBinding {
        enable = true
    }
    defaultConfig {
        applicationId = "com.example.familyhustle"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

    dependencies {
        implementation ("com.google.firebase:firebase-storage:21.0.0")

        implementation(platform("com.google.firebase:firebase-bom:32.0.0")) // Firebase BOM za kontrolu verzija
            implementation("com.google.firebase:firebase-auth-ktx") // Firebase Authentication
            implementation("com.google.firebase:firebase-database-ktx") // Firebase Realtime Database
        implementation ("com.github.bumptech.glide:glide:4.15.1")
        annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
        implementation ("com.google.android.material:material:1.9.0")
        implementation ("androidx.core:core-splashscreen:1.0.1")

        implementation("androidx.core:core-ktx:1.10.1")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
        implementation("androidx.recyclerview:recyclerview:1.3.1")
        implementation("androidx.room:room-runtime:2.5.2")
        kapt("androidx.room:room-compiler:2.5.2")
        implementation("com.google.android.material:material:1.9.0")
    }



