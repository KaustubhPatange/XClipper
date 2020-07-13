// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath(GradleDependency.GRADLE_BUILD_TOOLS)
        classpath(GradleDependency.KOTLIN_PLUGIN)
        classpath(GradleDependency.SAFE_ARGS)
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
