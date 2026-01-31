package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.keyValidation
import dev.forkhandles.values.ComparableValue
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory

data class Property(
    val description: String,
    val type: PropertyType,
)

class PropertyKey private constructor(value: String): StringValue(value), ComparableValue<PropertyKey, String> {
    companion object: StringValueFactory<PropertyKey>(::PropertyKey, keyValidation)
}

enum class PropertyType { Boolean, Number, String, Secret }