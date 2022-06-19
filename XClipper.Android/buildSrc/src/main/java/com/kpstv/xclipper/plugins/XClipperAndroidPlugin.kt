package com.kpstv.xclipper.plugins

import AndroidConfig
import BuildTypeRelease
import BuildType
import GradlePluginId
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class XClipperAndroidPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        configureAndroid()
    }

    private fun Project.configureAndroid() {
        configureAndroidInfo()
        configureAndroidApplicationId()
        configureAndroidLibraryProject()
        configureKotlinProject()
    }

    private fun Project.configureAndroidInfo() {
        extensions.findByType<BaseExtension>()?.apply {
            compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
            buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)

            defaultConfig {
                vectorDrawables.useSupportLibrary = true
                minSdk = AndroidConfig.MIN_SDK_VERSION
                targetSdk = AndroidConfig.TARGET_SDK_VERSION
                versionCode = AndroidConfig.VERSION_CODE
                versionName = AndroidConfig.VERSION_NAME

                testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
            }

            testOptions {
                unitTests.isIncludeAndroidResources = true
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            buildTypes {
                getByName(BuildType.RELEASE) {
                    isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
                    )
                }
                create(BuildType.IAP)
            }

            // configure dirs for ksp
            buildTypes.forEach { buildType ->
                sourceSets.maybeCreate(buildType.name).apply {
                    kotlin.srcDir("build/generated/ksp/${buildType.name}/kotlin")
                }
            }
        }
    }

    private fun Project.configureAndroidApplicationId() {
        plugins.withId(GradlePluginId.ANDROID_APPLICATION) {
            extensions.findByType<BaseAppModuleExtension>()?.apply {
                defaultConfig {
                    applicationId = AndroidConfig.ID
                }
            }
        }
    }

    private fun Project.configureAndroidLibraryProject() {
        plugins.withId(GradlePluginId.ANDROID_LIBRARY) {
            extensions.findByType<LibraryExtension>()?.apply {
                buildFeatures.buildConfig = false
            }
        }
    }

    private fun Project.configureKotlinProject() {
        tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_1_8.toString()
            }
        }
    }
}