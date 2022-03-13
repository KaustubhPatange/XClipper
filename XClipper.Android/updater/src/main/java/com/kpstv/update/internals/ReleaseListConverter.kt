package com.kpstv.update.internals

import com.google.gson.reflect.TypeToken
import com.kpstv.update.Release
import com.kpstv.xclipper.extensions.GsonListConverter

internal object ReleaseListConverter : GsonListConverter<Release>(typeToken = object : TypeToken<List<Release>>() {})