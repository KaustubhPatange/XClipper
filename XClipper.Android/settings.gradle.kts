rootProject.name ="XClipper"
rootProject.buildFileName = "build.gradle.kts"

/*
 * Gradle doesn't support definitions to be accessed from settings.gradle.kts
 * This workaround is a modified version of https://github.com/gradle/gradle/issues/11090#issuecomment-641170537
 */
rootDir
    .walk()
    .maxDepth(2)
    .filter { file ->
        file.name != "buildSrc" && file != rootDir && file.isDirectory && file("${file.absolutePath}/build.gradle.kts").exists()
    }
    .map { it.absolutePath.replace("${rootDir.absolutePath}\\", "") }
    .map { it.replace("\\",":") }
    .map { File(it).name }
    .filter { it.isNotEmpty() }
    .forEach { include(":$it") }