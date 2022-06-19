plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
}

android {
    buildFeatures {
        viewBinding = true
        buildConfig = false
    }
}

dependencies {
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.ACTIVITY_KTX)
    implementation(LibraryDependency.GLIDE)
    implementation(LibraryDependency.LOCAL_BROADCAST_MANAGER)
    implementation(LibraryDependency.NAVIGATOR)
    api(LibraryDependency.COROUTINES_TASKS)

    kapt(LibraryDependency.GLIDE_COMPILER)
}