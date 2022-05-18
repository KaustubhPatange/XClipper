plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.KOTLIN_PARCELIZE)
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    buildTypes {
        defaultConfig {
            getByName(BuildType.DEBUG) {
                val applicationId = "${AndroidConfig.ID}.debug"
                buildConfigField("String","APPLICATION_ID", "\"${applicationId}\"")
            }
            getByName(BuildType.RELEASE) {
                val applicationId = AndroidConfig.ID
                buildConfigField("String","APPLICATION_ID", "\"${applicationId}\"")
            }

            buildConfigField("Integer","VERSION_CODE", "${AndroidConfig.VERSION_CODE}")
            buildConfigField("String","VERSION_NAME", "\"${AndroidConfig.VERSION_NAME}\"")
        }
    }
}

dependencies {
    implementation(project(ModuleDependency.CORE_PRIVATE))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))
    implementation(project(ModuleDependency.LIBRARY_ROUNDED_SHEET))

    api(LibraryDependency.CORE_KTX)
    api(LibraryDependency.APP_COMPAT)
    api(LibraryDependency.MATERIAL)

    implementation(LibraryDependency.NAVIGATOR)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.ANDROIDX_PREFERENCES)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.COROUTINES_CORE)
    implementation(LibraryDependency.LIFECYCLE_KTX)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.LOTTIE)

    implementation(LibraryDependency.ROOM_KTX)
    implementation(LibraryDependency.RETROFIT_COROUTINES_ADAPTER)
    implementation(LibraryDependency.RETROFIT_GSON_CONVERTER)
    implementation(LibraryDependency.OKHTTP_LOGGING_INTERCEPTOR)
    implementation(LibraryDependency.TIMBER)
    implementation(LibraryDependency.FIREBASE_CRASHLYTICS)
    implementation(LibraryDependency.FIREBASE_ANALYTICS)
    implementation(LibraryDependency.PAGING)
    implementation(LibraryDependency.GIF_DRAWABLE)
    implementation(LibraryDependency.SENTRY)
    api(LibraryDependency.RETROFIT)

    implementation(kotlin("reflect"))

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)
}
