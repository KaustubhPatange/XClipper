plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.KOTLIN_PARCELIZE)
    id(GradlePluginId.DAGGER_HILT)
}

android {
    buildFeatures.viewBinding = true
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))
    implementation(project(ModuleDependency.CORE_PINLOCK))
    implementation(project(ModuleDependency.LIBRARY_ROUNDED_SHEET))

    implementation(LibraryDependency.FRAGMENT_KTX)
    implementation(LibraryDependency.LIFECYCLE_KTX)
    implementation(LibraryDependency.BILLING)
    implementation(LibraryDependency.NAVIGATOR)
    implementation(LibraryDependency.LOTTIE)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.WORK_MANAGER)
    implementation(LibraryDependency.WORK_MANAGER)

    implementation(LibraryDependency.HILT_WORK_MANAGER)
    kapt(LibraryDependency.HILT_WORK_MANAGER_COMPILER)

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)
}