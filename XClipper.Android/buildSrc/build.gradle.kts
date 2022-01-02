plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `kotlin-dsl-precompiled-script-plugins`
}

gradlePlugin {
    plugins {
        create("xclipperAndroid") {
            id = "com.kpstv.xclipper"
            implementationClass = "com.kpstv.xclipper.plugins.XClipperAndroidPlugin"
        }
    }
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    implementation("com.android.tools.build:gradle:7.0.2")
    implementation(gradleApi())
}