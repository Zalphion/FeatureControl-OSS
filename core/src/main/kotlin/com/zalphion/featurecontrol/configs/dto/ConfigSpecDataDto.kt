package com.zalphion.featurecontrol.configs.dto

import com.zalphion.featurecontrol.configs.Property
import com.zalphion.featurecontrol.configs.PropertyKey

data class ConfigSpecDataDto(
    val properties: Map<PropertyKey, Property>
)