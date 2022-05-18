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

    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.RETROFIT)
    implementation(LibraryDependency.RETROFIT_GSON_CONVERTER)
    implementation(LibraryDependency.ROOM_KTX)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.CWT)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.NAVIGATOR)
    implementation(LibraryDependency.ANDROIDX_PREFERENCES)
    
    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)
}
