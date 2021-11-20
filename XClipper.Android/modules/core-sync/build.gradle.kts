plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
}

android {
    buildFeatures.buildConfig = false
    buildTypes {
        getByName(BuildType.DEBUG) {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(ModuleDependency.CORE))
    implementation(project(ModuleDependency.CORE_PRIVATE))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.HVLOG)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.HILT_ANDROID)
}
