plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
}

android {
    buildFeatures.buildConfig = false
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.RETROFIT)
    implementation(LibraryDependency.RETROFIT_GSON_CONVERTER)
    implementation(LibraryDependency.ROOM_KTX)
    implementation(LibraryDependency.AUTO_BINDINGS)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.CWT)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.HILT_ANDROID)

    kapt(LibraryDependency.HILT_COMPILER) // TODO: Once everything settles try removing this
    kapt(LibraryDependency.AUTO_BINDINGS_COMPILER)
}
