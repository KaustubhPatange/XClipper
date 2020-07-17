// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlin_version by extra("1.3.72")
    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath(GradleDependency.GRADLE_BUILD_TOOLS)
        classpath(GradleDependency.KOTLIN_PLUGIN)
        classpath(GradleDependency.SAFE_ARGS)
        "classpath"("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven { url = uri("https://jitpack.io")}
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }

    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}
