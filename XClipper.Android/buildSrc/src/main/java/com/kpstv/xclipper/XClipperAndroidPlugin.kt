package com.kpstv.xclipper


import AndroidConfig
import BuildTypeRelease
import BuildType
import GradlePluginId
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

class XClipperAndroidPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        configureAndroid()
    }

    private fun Project.configureAndroid() {
        configureAndroidInfo()
        configureAndroidApplicationId()
    }

    private fun Project.configureAndroidInfo() {
        extensions.findByType<BaseExtension>()?.apply {
            compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
            buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)

            defaultConfig {
                vectorDrawables.useSupportLibrary = true
                minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
                targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
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
            }
        }
    }

    private fun Project.configureAndroidApplicationId() {
        plugins.withId(GradlePluginId.ANDROID_APPLICATION){
            extensions.findByType<BaseAppModuleExtension>()?.apply {
                defaultConfig {
                    applicationId = AndroidConfig.ID
                }
            }
        }
    }
}