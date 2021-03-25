package extensions

import com.android.build.api.dsl.BaseFlavor
import org.gradle.api.Project
import java.util.*

fun BaseFlavor.stringField(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
}

@Suppress("UNCHECKED_CAST")
fun <T> Project.loadProperty(name: String, default: T) : T {
    val properties = Properties().apply {
        load(rootProject.file(".gradle/gradle.properties").reader())
    }
    return (properties.getProperty(name) as? T) ?: default
}