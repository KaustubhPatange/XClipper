plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.KOTLIN_PARCELIZE)
    id(GradlePluginId.DAGGER_HILT)
}

android {
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))
    implementation(project(ModuleDependency.CORE_SPECIAL))
    implementation(project(ModuleDependency.LIBRARY_ROUNDED_SHEET))

    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.CWT)
    implementation(LibraryDependency.FRAGMENT_KTX)
    implementation(LibraryDependency.NAVIGATOR)

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)
}
