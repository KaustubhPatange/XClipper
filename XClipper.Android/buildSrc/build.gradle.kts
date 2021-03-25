plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("xclipperAndroid") {
            id = "com.kpstv.xclipper"
            implementationClass = "com.kpstv.xclipper.XClipperAndroidPlugin"
        }
    }
}

repositories {
    jcenter()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:4.1.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
    implementation(gradleApi())
}