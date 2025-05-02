plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "vcmsa.projects.personalbudgettingcorp"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "vcmsa.projects.personalbudgettingcorp"
        minSdk = 32
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.cast.framework)
    implementation(libs.firebase.perf.ktx)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Core Android libraries
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    // Room database
    implementation ("androidx.room:room-runtime:2.5.2")
    implementation ("androidx.room:room-ktx:2.5.2")

    // ViewModel and LiveData
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    //For async operations
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Navigation component
    implementation ("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation ("androidx.navigation:navigation-ui-ktx:2.6.0")

    // Firebase (for online database)
    implementation ("com.google.firebase:firebase-auth-ktx:22.1.0")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.7.0")
    implementation ("com.google.firebase:firebase-storage-ktx:20.2.1")

    // Chart library
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // For date picker
    implementation ("com.wdullaer:materialdatetimepicker:4.2.3")

    // MPAndroidChart for graphs
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation(files("libs/bcrypt-2.0.0.jar"))

    //For image loading
    implementation ("com.github.bumptech.glide:glide:4.15.1")

    // Testing libraries
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    dependencies {
        // ... other dependencies

        // CameraX core library
        implementation("androidx.camera:camera-core:1.3.1")
        // CameraX camera2 extensions
        implementation("androidx.camera:camera-camera2:1.3.1")
        // CameraX lifecycle extensions
        implementation("androidx.camera:camera-lifecycle:1.3.1")

        // Kotlin coroutines for CameraX
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

        // Material Design for Bottom Navigation
        implementation("com.google.android.material:material:1.11.0")

        // Glide for image loading
        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

        // Firebase Storage for image uploads
        implementation("com.google.firebase:firebase-storage-ktx")
    }
}