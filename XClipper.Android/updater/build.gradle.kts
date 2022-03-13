plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.WORK_MANAGER)
}