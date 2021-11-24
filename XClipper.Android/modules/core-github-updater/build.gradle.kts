plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.XCLIPPER_ANDROID)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
}

android {
    defaultConfig {
        buildConfigField("String", AndroidConfig::VERSION_NAME.name, "\"${AndroidConfig.VERSION_NAME}\"")
    }
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
    implementation(project(ModuleDependency.CORE_EXTENSIONS))
    implementation(project(ModuleDependency.UPDATER))

    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.WORK_MANAGER)
    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.HILT_ANDROID)
    implementation(LibraryDependency.HILT_WORK_MANAGER)
    implementation(LibraryDependency.AUTO_BINDINGS)
    compileOnly(LibraryDependency.AUTO_BINDINGS_ROOM_NOOP)

    kapt(LibraryDependency.AUTO_BINDINGS_COMPILER)
}
