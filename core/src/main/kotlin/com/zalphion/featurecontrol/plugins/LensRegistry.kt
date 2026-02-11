package com.zalphion.featurecontrol.plugins

import org.http4k.core.Request
import org.http4k.lens.BodyLens
import kotlin.reflect.KClass

class LensRegistry private constructor(private val lenses: Map<KClass<*>, BodyLens<*>>) {

    constructor(): this(emptyMap())

    inline fun <reified T: Any> with(lens: BodyLens<T>) = with(T::class, lens)

    fun with(type: KClass<*>, lens: BodyLens<*>) = LensRegistry(lenses + (type to lens))

    inline operator fun <reified T: Any> invoke(request: Request) =
        invoke(T::class, request)

    @Suppress("UNCHECKED_CAST")
    operator fun <T: Any> invoke(type: KClass<T>, request: Request) =
        (lenses[type] as? BodyLens<T>)
        ?.invoke(request)
        ?: error("Could not find $type lens")

    operator fun plus(other: LensRegistry) = LensRegistry(lenses + other.lenses)

    operator fun plus(others: Collection<LensRegistry>) = others.fold(this) { acc, next ->
        acc + next
    }
}