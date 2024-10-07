plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
    id("kotlin-kapt")
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.easyremcontrol"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.easyremcontrol"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    implementation("androidx.compose.material3:material3:1.1.0")

    // SSH и SFTP
    implementation("com.jcraft:jsch:0.1.55")

    // Шифрование данных
    implementation("androidx.security:security-crypto:1.1.0-alpha03")

    // Coroutines для асинхронной работы
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    // Navigation для Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")

    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.1")

    // Если используешь Material2,3 то можно добавить
    //implementation("androidx.compose.material:material:1.5.1")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.compose.material:material-icons-extended:1.0.1")

    implementation("com.hierynomus:sshj:0.32.0")  // Зависимость SSHJ
    implementation("androidx.security:security-crypto:1.1.0-alpha03")

    // Для сериализации данных
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Зависимости для юнит-тестов
    testImplementation("junit:junit:4.13.2")

    // Зависимости для тестов Android
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

}
