package com.zalphion.featurecontrol.lib

import org.http4k.format.AutoMarshalling
import org.http4k.lens.BiDiMapping
import kotlin.reflect.KClass

fun <T: Any> AutoMarshalling.asBiDiMapping(type: KClass<T>) = BiDiMapping(
    clazz = type.java,
    asOut = { asA(it, type) },
    asIn = { asFormatString(it) }
)

inline fun <reified T: Any> AutoMarshalling.asBiDiMapping() = BiDiMapping<String, T>(
    asOut = { asA(it) },
    asIn = { asFormatString(it) }
)

inline fun <reified T: Any> BiDiMapping<String, Array<T>>.mapToList() = map({ it.toList() }, { it.toTypedArray() })