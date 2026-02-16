package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.applications.Environment
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class EnvironmentDto(
    val name: EnvironmentName,
    val description: String = "",
    val colour: Colour = Colour.parse("#FFFFFF") // white
) {
    companion object
}

fun EnvironmentDto.toModel() = Environment(name, description, colour, emptyMap())

fun Environment.toDto() =
    EnvironmentDto(name, description, colour)