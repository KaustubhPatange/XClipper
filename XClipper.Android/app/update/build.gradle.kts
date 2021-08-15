plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(LibraryDependency.KOTLIN_STDLIB)
    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.COROUTINES_ANDROID)
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.WORK_MANAGER)

    compileOnly(LibraryDependency.AUTO_BINDINGS_ROOM_NOOP)
    implementation(LibraryDependency.AUTO_BINDINGS)
    kapt(LibraryDependency.AUTO_BINDINGS_COMPILER)
}