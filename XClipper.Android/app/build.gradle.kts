plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")

    defaultConfig {
        applicationId = "com.kpstv.xclipper"
        vectorDrawables.useSupportLibrary = true
       /* generatedDensities = []*/
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    /*
    kotlinOptions {
        jvmTarget = "1.8"
    }*/

   /* buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
        }
    }*/
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}


dependencies {
/*    def room_version = "2.2.5"
    def kodein_version = "6.2.1"
    def lifecycle_version = "2.2.0"
    def lottieVersion = "3.4.0"
    def kotlin_version = "1.3.72"
    def paging_version = "2.1.2"
    def nav_version = "2.3.0-rc01"*/

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.collection:collection-ktx:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    implementation(project(":app:license"))

    // Google Material Design
    implementation("com.google.android.material:material:1.2.0-beta01")

    // Google play core
    implementation("com.google.android.play:core:1.7.3")
    implementation("com.google.android.play:core-ktx:1.7.0")

    // Kotlin Android Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7")

    // Gson
    implementation("com.google.code.gson:gson:2.8.6")

    // Room
    implementation("androidx.room:room-runtime:2.2.5")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.firebase:firebase-database-ktx:19.3.1")
    kapt("androidx.room:room-compiler:2.2.5")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0-rc01")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0-rc01")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:2.1.2")

    // LifeCycle Service
    implementation("androidx.lifecycle:lifecycle-service:2.2.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.2.0")

    // Kodein
    implementation("org.kodein.di:kodein-di-generic-jvm:6.2.1")
    implementation("org.kodein.di:kodein-di-framework-android-x:6.2.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")/* {
        exclude (module: "okhttp")
    }*/
    implementation("com.squareup.okhttp3:okhttp:4.7.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.7.2")
    implementation("com.squareup.retrofit2:converter-gson:2.7.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

    // Simple Search View
    implementation("com.github.Ferfalk:SimpleSearchView:0.1.5")

    // Preference
    implementation("androidx.preference:preference:1.1.1")

    // Flexbox Layout
    implementation("com.google.android:flexbox:2.0.1")

    // Lottie
    implementation("com.airbnb.android:lottie:3.4.0")

    // Zxing QR code
    implementation("com.journeyapps:zxing-android-embedded:4.1.0")

    // Toasty
    implementation("com.github.GrenderG:Toasty:1.4.2")

    // Rounded Bottom Sheet
    implementation("com.github.Deishelon:RoundedBottomSheet:1.0.1")

    // Floating bubble
    implementation("com.github.bijoysingh:floating-bubble:3.0.0")

    // Android Gif Drawable
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.19")

    // Youtube Player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:10.0.5")

    debugImplementation("com.amitshekhar.android:debug-db:1.0.4")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
