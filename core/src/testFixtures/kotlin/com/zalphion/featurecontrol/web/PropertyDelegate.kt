package com.zalphion.featurecontrol.web

import kotlin.reflect.KProperty

interface PropertyDelegate<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T?
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?)
}