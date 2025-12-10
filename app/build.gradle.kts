plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    id("com.google.devtools.ksp") // KSP for Hilt
    id("com.google.dagger.hilt.android") // Hilt
    kotlin("kapt") // KAPT for Glide

    id("kotlin-parcelize") // <-- Add this
}


android {
    namespace = "com.example.helphive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.helphive"
        minSdk = 26
        targetSdk = 36
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
        viewBinding = true
    }
}



dependencies {


        implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    implementation("com.google.code.gson:gson:2.10.1")


    implementation(libs.firebase.auth)
        implementation(libs.firebase.database)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

        // Hilt
        implementation("com.google.dagger:hilt-android:2.57.2")
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    ksp("com.google.dagger:hilt-compiler:2.57.2")

        // AndroidX
        implementation("androidx.fragment:fragment-ktx:1.6.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
        implementation(libs.androidx.lifecycle.livedata.ktx)
        implementation("com.google.android.material:material:1.12.0")

        // Firebase
        implementation(libs.firebase.firestore)

        // MPAndroidChart (fixed)
        implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

            // ... existing dependencies ...
            implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

        // Glide (fixed)
        implementation("com.github.bumptech.glide:glide:4.16.0")
        kapt("com.github.bumptech.glide:compiler:4.16.0")

        // Others
        implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:1.9.10")
        implementation(libs.androidx.credentials)
        implementation(libs.androidx.credentials.play.services.auth)
        implementation(libs.googleid)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        implementation("androidx.security:security-crypto:1.1.0")




    // Offline persistence (Room instead of SharedPreferences for large data)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    
    }


