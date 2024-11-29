plugins {
    id("com.android.application") // Apply the plugin without a version
}

android {
    namespace = "com.example.mybookshelf"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mybookshelf"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.github.bumptech.glide:glide:4.15.1")  // Glide dependency
    implementation("androidx.appcompat:appcompat:1.6.1")      // AppCompat dependency
    implementation("com.google.android.material:material:1.9.0") // Material components
}
