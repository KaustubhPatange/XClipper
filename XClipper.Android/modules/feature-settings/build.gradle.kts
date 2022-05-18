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
    implementation(project(ModuleDependency.CORE_CLIPBOARD))
    implementation(project(ModuleDependency.CORE_SYNC))
    implementation(project(ModuleDependency.CORE_ADDONS))

    implementation(LibraryDependency.FRAGMENT_KTX)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.SWIPE_REFRESH_LAYOUT)
    implementation(LibraryDependency.ANDROIDX_PREFERENCES)
    implementation(LibraryDependency.NAVIGATOR)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.LOCAL_BROADCAST_MANAGER)
    implementation(LibraryDependency.LIFECYCLE_KTX)

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)
}
