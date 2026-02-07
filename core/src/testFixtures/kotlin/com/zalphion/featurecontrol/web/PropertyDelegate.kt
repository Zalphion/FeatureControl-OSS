package com.zalphion.featurecontrol.web

import kotlin.reflect.KProperty

interface PropertyDelegate<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T?
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?)
}

fun <T> PropertyDelegate<T>.afterSetValue(fn: (T?) -> Unit) = object: PropertyDelegate<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = this@afterSetValue.getValue(thisRef, property)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this@afterSetValue.setValue(thisRef, property, value)
        fn(value)
    }
}