import kotlin.reflect.full.memberProperties

@Suppress("unused")
object ModuleDependency {
    const val APP = ":app"
    const val LIBRARY_UTILS = ":app:library_utils"
    const val PRICING_CARDS = ":app:pricing"
    const val LINK_PREVIEW = ":app:link-preview"
    const val UPDATE = ":app:update"
    const val PIN_LOCK = ":app:pin-lock"


    const val CORE_EXTENSIONS = ":modules:core-extension"

    fun getAllModules(): Set<String> = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()
}