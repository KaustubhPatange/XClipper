plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin("kapt")
}

android {
    buildFeatures {
        viewBinding = true
        buildConfig = false
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.MATERIAL)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.ACTIVITY_KTX)
    implementation(LibraryDependency.GLIDE)
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")

    kapt(LibraryDependency.GLIDE_COMPILER)
}