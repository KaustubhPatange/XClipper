object AndroidConfig {
    const val COMPILE_SDK_VERSION = 33
    const val MIN_SDK_VERSION = 21
    const val TARGET_SDK_VERSION = 33
    const val BUILD_TOOLS_VERSION = "30.0.3" // deprecated

    const val VERSION_CODE = 41
    const val VERSION_NAME = "1.3.6"

    const val ID = "com.kpstv.xclipper"
    const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
}

interface BuildType {

    companion object {
        const val RELEASE = "release"
        const val DEBUG = "debug"
        const val IAP = "iap"
    }

    val isMinifyEnabled: Boolean
}

object BuildTypeRelease : BuildType {
    override val isMinifyEnabled = false
}
