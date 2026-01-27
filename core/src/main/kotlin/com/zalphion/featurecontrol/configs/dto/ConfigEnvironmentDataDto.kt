package com.zalphion.featurecontrol.configs.dto

import com.zalphion.featurecontrol.configs.PropertyKey

data class ConfigEnvironmentDataDto(
    val properties: Map<PropertyKey, String>
)