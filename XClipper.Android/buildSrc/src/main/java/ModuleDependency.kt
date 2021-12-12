import kotlin.reflect.full.memberProperties

@Suppress("unused")
object ModuleDependency {
    const val APP = ":app"
    const val PIN_LOCK = ":app:pin-lock"

    const val UPDATER = ":updater"

    const val CORE = ":modules:core"
    const val CORE_SYNC = ":modules:core-sync"
    const val CORE_PRIVATE = ":modules:core-private"
    const val CORE_EXTENSIONS = ":modules:core-extension"
    const val CORE_CLIPBOARD = ":modules:core-clipboard"
    const val CORE_GITHUB_UPDATER = ":modules:core-github-updater"
    const val CORE_SPECIAL = ":modules:core-special"

    const val FEATURE_ONBOARDING = ":modules:feature-onboarding"
    const val FEATURE_XCOPY = ":modules:feature-xcopy"
    const val FEATURE_SPECIAL = ":modules:feature-special"
    const val FEATURE_QUICKSETTINGS = ":modules:feature-quicksettings"

    fun getAllModules(): Set<String> = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()
}