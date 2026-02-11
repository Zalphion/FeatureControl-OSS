package com.zalphion.featurecontrol.plugins

import kotlinx.html.FlowContent
import kotlin.reflect.KClass

class ComponentRegistry private constructor(private val components: Map<KClass<*>, Component<*>>) {

    constructor(): this(emptyMap())

    inline fun <reified T: Any> with(component: Component<T>) = with(T::class, component)

    fun <T: Any> with(type: KClass<T>, component: Component<T>) = ComponentRegistry(components + (type to component))

    inline operator fun <reified T: Any> invoke(flow: FlowContent, data: T) =
        invoke(T::class, flow, data)

    @Suppress("UNCHECKED_CAST")
    operator fun <T: Any> invoke(type: KClass<T>, flow: FlowContent, data: T) =
        (components[type] as? Component<T>)
        ?.invoke(flow, data)
        ?: error("Could not find $type component")

    operator fun plus(other: ComponentRegistry) = ComponentRegistry(components + other.components)

    operator fun plus(others: Collection<ComponentRegistry>) = others.fold(this) { acc, next ->
        acc + next
    }
}

fun interface Component<T: Any>: (FlowContent, T) -> Unit