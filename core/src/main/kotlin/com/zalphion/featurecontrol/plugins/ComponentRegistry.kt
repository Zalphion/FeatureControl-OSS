package com.zalphion.featurecontrol.plugins

import kotlinx.html.FlowContent
import kotlin.reflect.KClass

class ComponentRegistry(private val components: List<ComponentContainer<*>>) {

    inline operator fun <reified T: Any> invoke(flow: FlowContent, data: T) =
        invoke(T::class, flow, data)

    operator fun <T: Any> invoke(type: KClass<T>, flow: FlowContent, data: T) = components
        .firstNotNullOfOrNull { it.getSafely(type) }
        ?.invoke(flow, data)
        ?: error("Could not find $type component")
}

class ComponentContainer<T: Any>(
    private val type: KClass<T>,
    private val component: Component<T>
) {
    fun <R: Any> getSafely(desiredType: KClass<R>): Component<R>? {
        return if (desiredType == type) {
            @Suppress("UNCHECKED_CAST")
            component as Component<R>
        } else null
    }
}

inline fun <reified T: Any> Component<T>.toContainer() = ComponentContainer(T::class, this)

fun interface Component<T: Any>: (FlowContent, T) -> Unit