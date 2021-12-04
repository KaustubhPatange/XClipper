plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
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
    implementation(project(ModuleDependency.CORE_SPECIAL))

    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.TOASTY)

    implementation(LibraryDependency.HILT_ANDROID)
}
