plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
}

android {
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(ModuleDependency.CORE))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
}
