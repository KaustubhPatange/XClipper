plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.DAGGER_HILT)
}

android {
    buildFeatures.buildConfig = false
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))
    implementation(project(ModuleDependency.CORE_CLIPBOARD))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.WORK_MANAGER)

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)

    implementation(LibraryDependency.HILT_WORK_MANAGER)
    kapt(LibraryDependency.HILT_WORK_MANAGER_COMPILER)
}
