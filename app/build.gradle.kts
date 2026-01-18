plugins {
    id("com.android.application")
}

android {
    namespace = "com.activities.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.activities.myapplication"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.navigation:navigation-fragment:2.8.7")
    implementation("androidx.navigation:navigation-ui:2.8.7")
    implementation(project(mapOf("path" to ":intent-guard")))
    implementation(project(mapOf("path" to ":intent-guard")))
//    implementation("com.github.ahamefuna1238:IntentGuard:v1.0")
}
