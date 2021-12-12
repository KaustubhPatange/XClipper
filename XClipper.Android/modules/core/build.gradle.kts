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
            var applicationId = AndroidConfig.ID
            getByName(BuildType.DEBUG) {
                applicationId += ".debug"
            }
            buildConfigField("String","APPLICATION_ID", "\"${applicationId}\"")
            buildConfigField("Integer","VERSION_CODE", "${AndroidConfig.VERSION_CODE}")
        }
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(ModuleDependency.CORE_PRIVATE))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.NAVIGATOR)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.ANDROIDX_PREFERENCES)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.COROUTINES_CORE)
    implementation(LibraryDependency.LIFECYCLE_KTX)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)

    implementation(LibraryDependency.ROOM_KTX)
    implementation(LibraryDependency.RETROFIT)
    implementation(LibraryDependency.RETROFIT_COROUTINES_ADAPTER)
    implementation(LibraryDependency.RETROFIT_GSON_CONVERTER)
    implementation(LibraryDependency.OKHTTP_LOGGING_INTERCEPTOR)
    implementation(LibraryDependency.TIMBER)
    implementation(LibraryDependency.FIREBASE_CRASHLYTICS)
    implementation(LibraryDependency.FIREBASE_ANALYTICS)
    implementation(LibraryDependency.PAGING)
    implementation(LibraryDependency.GIF_DRAWABLE)
    api(LibraryDependency.ROUND_BOTTOM_SHEET)

    implementation(kotlin("reflect"))

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)

    implementation(LibraryDependency.AUTO_BINDINGS)
    kapt(LibraryDependency.AUTO_BINDINGS_COMPILER)
}
