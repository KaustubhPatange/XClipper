package com.kpstv.xclipper.extensions

import java.util.HashMap
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

/**
 * If there is two same classes like one maybe domain & other may be entity,
 * following method might help to map such instance to other classes.
 *
 * Source: https://gist.github.com/KaustubhPatange/089792e18c19783247bb5b554e4ccaee
 */
inline fun <reified F : Any, reified T : Any> mapToClass(from: F, convertType: (String, Any?) -> Any? = { _,v -> v }): T {
    val args = HashMap<KParameter, Any?>()
    val params: List<KParameter> = T::class.constructors.first().parameters
    F::class.memberProperties.forEach { prop: KProperty1<out F, Any?> ->
        if (prop.visibility == KVisibility.PUBLIC) {
            val kParam: KParameter = params.first { it.name == prop.name }
            val value: Any? = prop.getter.call(from)
            args[kParam] = convertType.invoke(kParam.name!!, value)
        }
    }
    return T::class.constructors.first().callBy(args)
}