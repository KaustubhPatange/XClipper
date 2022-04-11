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
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.LOCAL_BROADCAST_MANAGER)

    implementation("com.github.traex.rippleeffect:ripple:1.3.1-OG")
    implementation("com.github.omadahealth.typefaceview:typefaceview:1.5.0@aar")
}
