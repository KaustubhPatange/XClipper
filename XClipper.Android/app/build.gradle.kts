import extensions.loadProperty
import extensions.stringField

plugins {
    id(GradlePluginId.ANDROID_APPLICATION)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.KOTLIN_PARCELIZE)
    id(GradlePluginId.DAGGER_HILT)
    id(GradlePluginId.GOOGLE_SERVICE)
    id(GradlePluginId.CRASHLYTICS)
    id(GradlePluginId.KSP) version GradlePluginVersion.KSP
}

android {
    buildFeatures.viewBinding = true

    sourceSets.getByName("main") {
        java.setSrcDirs(listOf("src/main/kotlin"))
    }
    sourceSets.getByName("debug").assets.srcDirs(files("$projectDir/schemas")) // Room

    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    signingConfigs {
        create(BuildType.RELEASE) {
            storeFile = rootProject.file("key.jks")
            storePassword = loadProperty("storePassword", "")
            keyAlias = loadProperty("keyAlias", "")
            keyPassword = loadProperty("keyPassword", "")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildTypes {
        defaultConfig {
            stringField("SERVER_URI", loadProperty("SERVER_URI", ""))
        }
        getByName(BuildType.RELEASE) {
//            isMinifyEnabled = true // R8 Works but still needs more testing (PS: ClipboardAccessibilityService does not work)
//            isShrinkResources = true
            signingConfig = signingConfigs.getByName(BuildType.RELEASE)
        }
        getByName(BuildType.DEBUG) {
//            isMinifyEnabled = true // Test on debug mode to find any issues.
//            isShrinkResources = true
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        create(BuildType.IAP) {
            initWith(getByName(BuildType.DEBUG))
            matchingFallbacks.add(BuildType.DEBUG)
            applicationIdSuffix = applicationIdSuffix?.removePrefix(".debug")
        }
    }
}

tasks.register("checkForChangelog") {
    doFirst {
        val versionCode = project.android.defaultConfig.versionCode
        val file = File("$rootDir\\fastlane\\metadata\\android\\en-US\\changelogs\\${versionCode}.txt")
        if (!file.exists()) {
            throw BuildCancelledException("Error: Please define a changelog for the versionCode $versionCode at \"fastlane\\metadata\\android\\en-US\\changelogs\"")
        }
    }
}

dependencies {
    for (moduleId in ModuleDependency.getAllModules().filterNot { it == ModuleDependency.APP })
        implementation(project(moduleId))
    implementation(LibraryDependency.APP_COMPAT)
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

    implementation(LibraryDependency.BILLING)
    implementation(LibraryDependency.FIREBASE_REALTIME_DATABASE)
    implementation(LibraryDependency.FIREBASE_AUTH)
    implementation(LibraryDependency.FIREBASE_CRASHLYTICS)
    implementation(LibraryDependency.FIREBASE_ANALYTICS)
    implementation(LibraryDependency.PLAY_SERVICE_AUTH)

    implementation(LibraryDependency.PAGING)
    implementation(LibraryDependency.LIFECYCLE_EXTENSIONS)
    implementation(LibraryDependency.LIFECYCLE_VIEWMODEL)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.LIFECYCLE_KTX)
    implementation(LibraryDependency.LIVEDATA_COMBINE_UTIL)

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
    implementation(LibraryDependency.REALTIME_EXTENSIONS)
    implementation(LibraryDependency.TIMBER)
    implementation(LibraryDependency.MARKWON)
    implementation(LibraryDependency.PINLOCK)

    implementation(LibraryDependency.NAVIGATOR)
    implementation(LibraryDependency.NAVIGATOR_EXTENSIONS)

    implementation(LibraryDependency.HILT_ANDROID)
    implementation(LibraryDependency.HILT_WORK_MANAGER)

    kapt(LibraryDependency.HILT_COMPILER)
    kapt(LibraryDependency.HILT_WORK_MANAGER_COMPILER)

    kapt(LibraryDependency.ROOM_COMPILER_KAPT)

    testImplementation(TestLibraryDependency.JUNIT)
    androidTestImplementation(LibraryDependency.ROOM_TESTING)
    androidTestImplementation(TestLibraryDependency.JUNIT_TEST_EXT)
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation(TestLibraryDependency.ESPRESSO_CORE)

    implementation(kotlin("reflect"))
}

tasks.withType<Test>() {
    useJUnitPlatform()
}