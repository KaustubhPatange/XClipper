buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath(GradleDependency.GRADLE_BUILD_TOOLS)
        classpath(GradleDependency.KOTLIN_PLUGIN)
        classpath(GradleDependency.KSP_PLUGIN)
        classpath(GradleDependency.DAGGER_HILT)
        classpath(GradleDependency.CRASHLYTICS)
        classpath(GradleDependency.GOOGLE_SERVICE)
        classpath(GradleDependency.SPOTIFY_RULER)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io")}
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://github.com/omadahealth/omada-nexus/raw/master/release") }
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
        kotlinOptions {
            freeCompilerArgs += listOf(
                "-Xopt-in=kotlin.ExperimentalStdlibApi",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}
