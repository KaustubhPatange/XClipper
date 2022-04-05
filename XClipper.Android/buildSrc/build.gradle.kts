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
    implementation("com.android.tools.build:gradle:7.1.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    implementation(gradleApi())
}