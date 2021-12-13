plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
}

android {
    buildFeatures {
        buildConfig = false
        viewBinding = true
    }
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(project(ModuleDependency.CORE_CLIPBOARD))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.LIFECYCLE_KTX)
    implementation(LibraryDependency.FRAGMENT_KTX)
    implementation(LibraryDependency.NAVIGATOR)
    implementation(LibraryDependency.HILT_ANDROID)
    implementation(LibraryDependency.GIF_DRAWABLE)
}
