plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
}

android {
    buildFeatures.buildConfig = false
}

dependencies {
    implementation(LibraryDependency.CORE_KTX)
}
