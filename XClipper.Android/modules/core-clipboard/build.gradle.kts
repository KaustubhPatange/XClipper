plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
}

android {
    buildFeatures.buildConfig = true
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(LibraryDependency.CORE_KTX)
}
