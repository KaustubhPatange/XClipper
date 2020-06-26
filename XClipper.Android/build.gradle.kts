/*
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android-extensions")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}
*/

plugins {
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")
}

buildscript {
    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath(GradlePluginGroup.GRADLE_BUILD_TOOLS, GradlePluginName.GRADLE, GradlePluginVersion.ANDROID_GRADLE)
        classpath(GradlePluginGroup.GOOGLE_SERVICE, GradlePluginName.GMS, GradlePluginVersion.GMS)
        classpath(GradlePluginGroup.KOTLIN_PLUGIN, GradlePluginName.KOTLIN_GRADLE, GradlePluginVersion.KOTLIN)
        classpath(GradlePluginGroup.SAFE_ARGS, GradlePluginName.SAFE_ARGS, GradlePluginVersion.SAFE_ARGS)
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

/*subprojects {
    plugins.withType<BasePlugin> {

    }
}*/

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}