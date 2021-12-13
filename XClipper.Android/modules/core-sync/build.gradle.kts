plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.DAGGER_HILT)
}

android {
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(project(ModuleDependency.CORE_CLIPBOARD)) // TODO: Try removing this & see if compiles then remove the dependency of FirebaseUtils from ClipboardAccessibilityService so that we can move it to core-clipboard.
    implementation(project(ModuleDependency.CORE_EXTENSIONS))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.ACTIVITY_KTX)
    implementation(LibraryDependency.FRAGMENT_KTX)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.HVLOG)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.LOTTIE)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.ZXING_ANDROID_QR)

    implementation(LibraryDependency.FIREBASE_AUTH)
    implementation(LibraryDependency.PLAY_SERVICE_AUTH)

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)
}
