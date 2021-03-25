private object GradlePluginVersion {
    const val KOTLIN = CoreVersion.KOTLIN
    const val ANDROID_GRADLE = "4.1.1"
    const val GOOGLE_SERVICE = "4.3.3"
    const val SAFE_ARGS = CoreVersion.JETPACK_NAVIGATION
    const val HILT = "2.28-alpha"
}

object GradlePluginId {
    const val XCLIPPER_ANDROID = "com.kpstv.xclipper"
    const val ANDROID_APPLICATION = "com.android.application"
    const val ANDROID_LIBRARY = "com.android.library"
    const val ANDROID_KTX = "android"
    const val ANDROID_EXTENSIONS_KTX = "android.extensions"
    const val KAPT = "kapt"
    const val GOOGLE_SERVICE = "com.google.gms.google-services"
    const val SAFE_ARGS = "androidx.navigation.safeargs.kotlin"
    const val DAGGER_HILT = "dagger.hilt.android.plugin"
}

object GradleDependency {
    const val GRADLE_BUILD_TOOLS = "com.android.tools.build:gradle:${GradlePluginVersion.ANDROID_GRADLE}"
    const val KOTLIN_PLUGIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${GradlePluginVersion.KOTLIN}"
    const val GOOGLE_SERVICE = "com.google.gms:google-services:${GradlePluginVersion.GOOGLE_SERVICE}"
    const val SAFE_ARGS = "androidx.navigation:navigation-safe-args-gradle-plugin:${GradlePluginVersion.SAFE_ARGS}"
    const val DAGGER_HILT = "com.google.dagger:hilt-android-gradle-plugin:${GradlePluginVersion.HILT}"
}