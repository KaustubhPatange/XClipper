import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id(GradlePluginId.ANDROID_APPLICATION)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.ANDROID_EXTENSIONS_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.SAFE_ARGS)
    id(GradlePluginId.DAGGER_HILT)
}

android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
    buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)

    viewBinding.isEnabled = true

    defaultConfig {
        applicationId = AndroidConfig.ID
        vectorDrawables.useSupportLibrary = true
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME

        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }

    signingConfigs {
        val propertiesFile = rootProject.file("keystore.properties")
        val properties = Properties()
        properties.load(propertiesFile.reader())

        create(BuildType.RELEASE) {
            storeFile = rootProject.file("key.jks")
            storePassword = properties["storePassword"] as String
            keyAlias = properties["keyAlias"] as String
            keyPassword = properties["keyPassword"] as String
        }
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
            signingConfig = signingConfigs.getByName(BuildType.RELEASE)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName(BuildType.DEBUG) {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    for (moduleId in ModuleDependency.getAllModules().filterNot { it == ModuleDependency.APP })
        implementation(project(moduleId))
    implementation(LibraryDependency.KOTLIN_STDLIB)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.ACTIVITY_KTX)
    implementation(LibraryDependency.FRAGMENT_KTX)
    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.COLLECTIONS_KTX)
    implementation(LibraryDependency.RECYCLERVIEW)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.PLAY_CORE)
    implementation(LibraryDependency.PLAY_CORE_KTX)
    implementation(LibraryDependency.COROUTINES_CORE)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.WORK_MANAGER)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.ROOM_KTX)
    implementation(LibraryDependency.ROOM_RUNTIME)
    implementation(LibraryDependency.OKHTTP)

    implementation(LibraryDependency.FIREBASE_REALTIME_DATABASE)
    implementation(LibraryDependency.FIREBASE_AUTH)
    implementation(LibraryDependency.PLAY_SERVICE_AUTH)
    implementation(LibraryDependency.NAVIGATION_FRAGMENT)
    implementation(LibraryDependency.NAVIGATION_UI)
    implementation(LibraryDependency.PAGING)
    implementation(LibraryDependency.LIFECYCLE_EXTENSIONS)
    implementation(LibraryDependency.LIFECYCLE_VIEWMODEL)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.LIFECYCLE_KTX)

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
    implementation(LibraryDependency.HVLOG)
    implementation(LibraryDependency.GLIDE)
    implementation(LibraryDependency.CWT)
    implementation(LibraryDependency.AUTO_BINDINGS)
    implementation(LibraryDependency.REALTIME_EXTENSIONS)

    implementation(LibraryDependency.HILT_ANDROID)
    implementation(LibraryDependency.HILT_VIEWODEL)
    implementation(LibraryDependency.HILT_WORK_MANAGER)

    kapt(LibraryDependency.ROOM_COMPILER_KAPT)
    kapt(LibraryDependency.GLIDE_COMPILER)
    kapt(LibraryDependency.HILT_COMPILER)
    kapt(LibraryDependency.HILT_VIEWODEL_COMPILER)
    kapt(LibraryDependency.AUTO_BINDINGS_COMPILER)

    kapt(LibraryDependency.GLIDE_COMPILER)
    kapt(LibraryDependency.ROOM_COMPILER_KAPT)

    debugImplementation(TestLibraryDependency.ANDROID_DEBUG_DB)
    testImplementation(TestLibraryDependency.JUNIT)
    androidTestImplementation(TestLibraryDependency.JUNIT_TEST_EXT)
    androidTestImplementation(TestLibraryDependency.ESPRESSO_CORE)
    implementation(kotlin("reflect"))
}
