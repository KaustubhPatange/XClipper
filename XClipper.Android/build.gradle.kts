buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.kotlin.ksp.gradlePlugin)
        classpath(libs.android.hilt.gradlePlugin)
        classpath(libs.firebase.crashlytics.gradlePlugin)
        classpath(libs.google.gms.gradlePlugin)
        classpath(libs.spotify.ruler.gradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
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
