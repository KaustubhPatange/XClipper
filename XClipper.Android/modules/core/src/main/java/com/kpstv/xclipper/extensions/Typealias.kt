package com.kpstv.xclipper.extensions

import androidx.fragment.app.Fragment
import com.kpstv.xclipper.data.model.Dictionary
import kotlin.reflect.KClass

typealias SimpleFunction = () -> Unit
typealias ErrorFunction = (Exception?) -> Unit
typealias FragClazz = KClass<out Fragment>

typealias ClipTagMap = Dictionary<String, String>