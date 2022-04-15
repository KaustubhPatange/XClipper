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
    implementation(project(ModuleDependency.CORE_PINLOCK))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.PAGING)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.FLOATING_BUBBLE)
    implementation(LibraryDependency.LOCAL_BROADCAST_MANAGER)

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)
}
