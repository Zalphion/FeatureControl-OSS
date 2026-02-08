package com.zalphion.featurecontrol.plugins

import org.http4k.core.Request
import org.http4k.lens.BodyLens
import kotlin.reflect.KClass

class LensRegistry(private vararg val lenses: LensContainer<*>) {

    inline operator fun <reified T: Any> invoke(request: Request) =
        invoke(T::class, request)

    operator fun <T: Any> invoke(type: KClass<T>, request: Request) = lenses
        .firstNotNullOfOrNull { it.getSafely(type) }
        ?.invoke(request)
        ?: error("Could not find $type lens")

    operator fun plus(other: LensRegistry) = LensRegistry(*lenses, *other.lenses)
}

class LensContainer<T: Any>(
    private val type: KClass<T>,
    val lens: BodyLens<T>
) {
    fun <R: Any> getSafely(desiredType: KClass<R>): BodyLens<R>? {
        return if (desiredType == type) {
            @Suppress("UNCHECKED_CAST")
            lens as BodyLens<R>
        } else null
    }
}

inline fun <reified T: Any> BodyLens<T>.toContainer() = LensContainer(T::class, this)