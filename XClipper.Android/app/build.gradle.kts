plugins {
    id(GradlePluginId.ANDROID_APPLICATION)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.ANDROID_EXTENSIONS_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.SAFE_ARGS)
}

android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
    buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)

    defaultConfig {
        applicationId = AndroidConfig.ID
        vectorDrawables.useSupportLibrary = true
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME

        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    for (moduleId in ModuleDependency.getAllModules().filterNot { it == ModuleDependency.APP })
        implementation(project(moduleId))
    implementation(LibraryDependency.KOTLIN_STDLIB)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.ACTIVITY)
    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.COLLECTIONS_KTX)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.PLAY_CORE)
    implementation(LibraryDependency.PLAY_CORE_KTX)
    implementation(LibraryDependency.COROUTINES_CORE)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.ROOM_RUNTIME)
    kapt(LibraryDependency.ROOM_COMPILER_KAPT)

    implementation(LibraryDependency.FIREBASE_REALTIME_DATABASE)
    implementation(LibraryDependency.FIREBASE_AUTH)
    implementation(LibraryDependency.PLAY_SERVICE_AUTH)
    implementation(LibraryDependency.NAVIGATION_FRAGMENT)
    implementation(LibraryDependency.NAVIGATION_UI)
    implementation(LibraryDependency.PAGING)
    implementation(LibraryDependency.LIFECYCLE_EXTENSIONS)
    implementation(LibraryDependency.LIFECYCLE_VIEWMODEL)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.KODEIN_JVM)
    implementation(LibraryDependency.KODEIN_ANDROID)
    implementation(LibraryDependency.RETROFIT) {
        exclude("okhttp")
    }
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.OKHTTP_LOGGING_INTERCEPTOR)
    implementation(LibraryDependency.RETROFIT_GSON_CONVERTER)
    implementation(LibraryDependency.RETROFIT_COROUTINES_ADAPTER)
    implementation(LibraryDependency.SIMPLE_SEARCH_VIEW)
    implementation(LibraryDependency.ANDROIDX_PREFERENCES)
    implementation(LibraryDependency.FLEXBOX)
    implementation(LibraryDependency.LOTTIE)
    implementation(LibraryDependency.ZXING_ANDROID_QR)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.ROUND_BOTTOM_SHEET)
    implementation(LibraryDependency.FLOATING_BUBBLE)
    implementation(LibraryDependency.GIF_DRAWABLE)

    debugImplementation(TestLibraryDependency.ANDROID_DEBUG_DB)
    testImplementation(TestLibraryDependency.JUNIT)
    androidTestImplementation(TestLibraryDependency.JUNIT_TEST_EXT)
    androidTestImplementation(TestLibraryDependency.ESPRESSO_CORE)
}
