private object TestLibraryVersion {
    const val DEBUG_DB = "1.0.4"
    const val JUNIT = "4.13"
    const val JUNIT_TEST = "1.1.1"
    const val ESPRESSO = "3.2.0"
}

object TestLibraryDependency {
    const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:${TestLibraryVersion.ESPRESSO}"
    const val JUNIT_TEST_EXT = "androidx.test.ext:junit:${TestLibraryVersion.JUNIT_TEST}"
    const val JUNIT = "junit:junit:${TestLibraryVersion.JUNIT}"
    const val ANDROID_DEBUG_DB = "com.amitshekhar.android:debug-db:${TestLibraryVersion.DEBUG_DB}"
}