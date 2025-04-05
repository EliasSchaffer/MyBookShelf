plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.mybookshelf"

    defaultConfig {
        compileSdk = 34
        applicationId = "com.example.mybookshelf"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isJniDebuggable = true
            isRenderscriptDebuggable = true
            isProfileable = true
        }
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
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("mysql:mysql-connector-java:5.1.49")
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //implementation ("org.apache.commons:commons-dbcp2:2.9.0")
    //implementation ("commons-pool:commons-pool2:2.11.1")
    implementation(libs.appcompat)
    implementation(libs.material)
}
