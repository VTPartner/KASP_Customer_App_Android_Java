plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.gms)
    alias(libs.plugins.devtool.ksp) version "1.9.0-1.0.13"
}

android {
    namespace = "com.kapstranspvtltd.kaps"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.kapstranspvtltd.kaps"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 13
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
//        getByName("debug") {
//            applicationIdSuffix = ".debug"
//            isMinifyEnabled = false
//            isDebuggable = true
//        }
        getByName("release") {
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
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {


    implementation (libs.androidx.lifecycle.viewmodel)
    implementation (libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation (libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.gson)

    implementation (libs.dotsindicator)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.onesignal)
    implementation (libs.play.services.places)
    implementation (libs.play.services.location)
    implementation (libs.play.services.maps)
//    implementation 'com.google.firebase:firebase-auth:19.3.2'
//    implementation 'com.google.firebase:firebase-messaging:20.2.4'
//    implementation 'com.google.firebase:firebase-database:19.3.1'
    implementation (libs.logging.interceptor)
    implementation (libs.glide)

    //razor pay
    implementation (libs.checkout)

    //PolyUtil
    implementation (libs.android.maps.utils)
    //
    implementation (libs.slf4j.simple)
    implementation (libs.google.maps.services)

    //Shimmer
    implementation(libs.shimmer)

    // CircleImageView
    implementation (libs.circleimageview)

    //Language Change
//    implementation (libs.translate) // current version does not support 16 KB

    //RazorPay
    implementation (libs.checkout)

    //Lottie animation
    implementation (libs.lottie)
}