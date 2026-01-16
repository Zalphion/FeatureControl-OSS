package com.zalphion.featurecontrol.auth

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.features.EnvironmentName
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory

class EnginePrincipal private constructor(value: String): StringValue(value) {
    companion object: StringValueFactory<EnginePrincipal>(::EnginePrincipal) {
        fun of(appId: AppId, environmentName: EnvironmentName) = of("$appId:$environmentName")
    }

    val appId = AppId.parse(value.split(":").first())
    val environmentName = EnvironmentName.parse(value.split(":").last())
}