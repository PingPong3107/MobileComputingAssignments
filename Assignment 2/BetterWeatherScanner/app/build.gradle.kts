plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.unistuttgart.betterweatherscanner"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.unistuttgart.betterweatherscanner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val logTag: String by project
        val humidityCharacteristicUuid: String by project
        val temperatureCharacteristicUuid: String by project
        val weatherServiceUuid: String by project
        val lightServiceUuid: String by project
        val lightCharacteristicUuid: String by project
        val descriptorUuid: String by project

        buildConfigField("String", "LOG_TAG", "\"$logTag\"")
        buildConfigField("String", "HUMIDITY_CHARACTERISTIC_UUID", "\"$humidityCharacteristicUuid\"")
        buildConfigField("String", "TEMPERATURE_CHARACTERISTIC_UUID", "\"$temperatureCharacteristicUuid\"")
        buildConfigField("String", "WEATHER_SERVICE_UUID", "\"$weatherServiceUuid\"")
        buildConfigField("String", "LIGHT_SERVICE_UUID", "\"$lightServiceUuid\"")
        buildConfigField("String", "LIGHT_CHARACTERISTIC_UUID", "\"$lightCharacteristicUuid\"")
        buildConfigField("String", "DESCRIPTOR_UUID", "\"$descriptorUuid\"")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    //implementation(libs.androidx.constraintlayout)
    //implementation(libs.androidx.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}