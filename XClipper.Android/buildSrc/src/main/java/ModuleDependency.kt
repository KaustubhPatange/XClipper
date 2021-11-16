import kotlin.reflect.full.memberProperties

@Suppress("unused")
object ModuleDependency {
    const val APP = ":app"
    const val PRICING_CARDS = ":app:pricing"
    const val LINK_PREVIEW = ":app:link-preview"
    const val PIN_LOCK = ":app:pin-lock"

    const val UPDATER = ":updater"

    const val CORE_PRIVATE = ":modules:core-private"
    const val CORE_EXTENSIONS = ":modules:core-extension"

    fun getAllModules(): Set<String> = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()
}