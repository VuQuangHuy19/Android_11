plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.app6hu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app6hu"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // RecyclerView + Chart
    implementation(libs.recyclerview)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Room (nếu bạn thực sự dùng)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // CHỈ Firestore – vì bạn chỉ dùng database
    implementation("com.google.firebase:firebase-firestore")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
