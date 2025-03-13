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
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("mysql:mysql-connector-java:8.0.26")
    implementation ("org.mindrot:jbcrypt:0.4")
}
