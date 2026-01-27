package com.zalphion.featurecontrol.plugins

import kotlinx.html.FlowContent
import kotlin.reflect.KClass

class ComponentRegistry {
    private val components = mutableListOf<ComponentContainer<*>>()

    operator fun <T: Any> set(type: KClass<T>, component: Component<T>) {
        // add with higher precedence
        components.addFirst(ComponentContainer(type, component))
    }

    inline operator fun <reified T: Any> invoke(flow: FlowContent, data: T) =
        invoke(T::class, flow, data)

    operator fun <T: Any> invoke(type: KClass<T>, flow: FlowContent, data: T) = components
        .firstNotNullOfOrNull { it.getSafely(type) }
        ?.invoke(flow, data)
        ?: error("Could not find $type component")
}

private class ComponentContainer<T: Any>(
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

fun interface Component<T: Any>: (FlowContent, T) -> Unit