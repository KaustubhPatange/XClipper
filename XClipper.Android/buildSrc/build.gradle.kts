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
    implementation("com.android.tools.build:gradle:7.4.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
    implementation("com.squareup:javapoet:1.13.0")
    implementation(gradleApi())
}