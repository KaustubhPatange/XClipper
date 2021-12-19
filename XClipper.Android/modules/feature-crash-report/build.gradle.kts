import extensions.loadProperty

plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)

}

android {
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        manifestPlaceholders["sentry_dsn_value"] = loadProperty("SENTRY_DSN", "")
    }
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.ACTIVITY_KTX)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.LOTTIE)
    implementation(LibraryDependency.FIREBASE_CRASHLYTICS)
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.SENTRY)

}
