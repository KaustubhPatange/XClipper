object GradlePluginVersion {
    const val KOTLIN = CoreVersion.KOTLIN
    const val ANDROID_GRADLE = "7.1.0"
    const val GOOGLE_SERVICE = "4.3.10"
    const val CRASHLYTICS = "2.7.1"
    const val KSP = "1.5.31-1.0.0"
    const val SPOTIFY_RULER = "1.1.1"
}

object GradlePluginId {
    const val XCLIPPER_ANDROID = "com.kpstv.xclipper.plugins"
    const val XCLIPPER_KSP = "com.kpstv.xclipper.ksp"
    const val ANDROID_APPLICATION = "com.android.application"
    const val ANDROID_LIBRARY = "com.android.library"
    const val ANDROID_KTX = "android"
    const val KOTLIN_PARCELIZE = "kotlin-parcelize"
    const val KAPT = "kapt"
    const val GOOGLE_SERVICE = "com.google.gms.google-services"
    const val DAGGER_HILT = "dagger.hilt.android.plugin"
    const val CRASHLYTICS = "com.google.firebase.crashlytics"
    const val KSP = "com.google.devtools.ksp"
    const val SPOTIFY_RULER = "com.spotify.ruler"
    const val KOTLINX_SERIALIZATION = "kotlinx-serialization"
}

object GradleDependency {
    const val GRADLE_BUILD_TOOLS = "com.android.tools.build:gradle:${GradlePluginVersion.ANDROID_GRADLE}"
    const val KOTLIN_PLUGIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${GradlePluginVersion.KOTLIN}"
    const val GOOGLE_SERVICE = "com.google.gms:google-services:${GradlePluginVersion.GOOGLE_SERVICE}"
    const val DAGGER_HILT = "com.google.dagger:hilt-android-gradle-plugin:${CoreVersion.HILT}"
    const val CRASHLYTICS = "com.google.firebase:firebase-crashlytics-gradle:${GradlePluginVersion.CRASHLYTICS}"
    const val SPOTIFY_RULER = "com.spotify.ruler:ruler-gradle-plugin:${GradlePluginVersion.SPOTIFY_RULER}"
    const val KSP_PLUGIN = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${GradlePluginVersion.KSP}"
    const val KOTLINX_SERIALIZATION = "org.jetbrains.kotlin:kotlin-serialization:${GradlePluginVersion.KOTLIN}"
}