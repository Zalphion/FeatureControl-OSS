package com.zalphion.featurecontrol.plugins

import org.http4k.lens.BiDiLensSpec
import org.http4k.lens.LensGet
import org.http4k.lens.LensSet
import org.http4k.lens.ParamMeta

typealias Extensions = Map<String, String>

fun Extensions.with(vararg updates: (Extensions) -> Extensions) = updates.fold(this) { acc, next -> next(acc) }

fun extensions(vararg updates: (Extensions) -> Extensions) = emptyMap<String, String>().with(*updates)

interface Extendable<SELF: Extendable<SELF>> {
    val extensions: Extensions
    fun with(extensions: Extensions): SELF
}

object Extension: BiDiLensSpec<Extensions, String>("extensions", ParamMeta.StringParam,
    LensGet { name, target -> listOfNotNull(target[name]) },
    LensSet { name, values, target ->
        values.fold(target) { acc, next ->
            acc.plus(name to next)
        }
    }
) {
    inline fun <reified T: Extendable<T>> on() = BiDiLensSpec(
        location = T::class.simpleName ?: "extendable",
        paramMeta = ParamMeta.StringParam,
        get = LensGet { name, target: T -> listOfNotNull(target.extensions[name]) },
        set = LensSet { name, values, target ->
            values.fold(target) { acc, next ->
                acc.with(mapOf(name to next))
            }
        }
    )
}