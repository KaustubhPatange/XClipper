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
    implementation(project(ModuleDependency.CORE_CLIPBOARD))
    implementation(project(ModuleDependency.CORE_EXTENSIONS))

    implementation(LibraryDependency.ACTIVITY_KTX)
    implementation(LibraryDependency.FRAGMENT_KTX)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.RECYCLERVIEW)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.FLEXBOX)
    implementation(LibraryDependency.SIMPLE_SEARCH_VIEW)
    implementation(LibraryDependency.LIFECYCLE_KTX)
    implementation(LibraryDependency.LIFECYCLE_VIEWMODEL)
    implementation(LibraryDependency.NAVIGATOR)
    implementation(LibraryDependency.LIVEDATA_COMBINE_UTIL)
    implementation(LibraryDependency.PAGING)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.BALLOON)

    implementation(LibraryDependency.ROOM_KTX)
    kapt(LibraryDependency.ROOM_COMPILER_KAPT)

    implementation(LibraryDependency.HILT_ANDROID)
    kapt(LibraryDependency.HILT_COMPILER)
}
