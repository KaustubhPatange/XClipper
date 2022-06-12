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
        create("kspPlugin") {
            id = "com.kpstv.xclipper.ksp"
            implementationClass = "com.kpstv.xclipper.plugins.KSPPlugin"
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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation(gradleApi())
}