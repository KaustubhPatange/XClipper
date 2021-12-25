plugins {
  id(GradlePluginId.ANDROID_LIBRARY)
  id(GradlePluginId.XCLIPPER_ANDROID)
  kotlin(GradlePluginId.ANDROID_KTX)
}

dependencies {
  implementation(LibraryDependency.CORE_KTX)
}