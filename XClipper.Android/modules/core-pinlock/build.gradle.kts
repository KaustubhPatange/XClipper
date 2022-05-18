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
    implementation(project(ModuleDependency.CORE_EXTENSIONS))
    implementation(project(ModuleDependency.LIBRARY_PINLOCK))

    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.TOASTY)
}
