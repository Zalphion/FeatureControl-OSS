package com.zalphion.featurecontrol

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.auth.EnginePrincipal
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.SubjectId
import com.zalphion.featurecontrol.features.Weight
import com.zalphion.featurecontrol.crypto.Base64String
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.teams.TeamName
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.users.UserId
import org.http4k.format.AutoMappingConfiguration
import org.http4k.format.AutoMarshalling
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.value
import org.http4k.format.withStandardMappings
import se.ansman.kotshi.KotshiJsonAdapterFactory
import java.math.BigDecimal

@KotshiJsonAdapterFactory
private object CoreJsonAdapterFactory : JsonAdapter.Factory by KotshiCoreJsonAdapterFactory

internal fun buildJson(plugins: List<PluginFactory<*>>): AutoMarshalling = plugins
    .mapNotNull { it.jsonExport }
    .fold(Moshi.Builder()) { builder, export -> export.moshi(builder) }
    .add(CoreJsonAdapterFactory)
    .add(BigDecimalAdapter)
    .add(ListAdapter)
    .add(MapAdapter)
    .asConfigurable()
    .withStandardMappings()
    .value(TeamId)
    .value(AppId)
    .value(AppName)
    .value(FeatureKey)
    .value(Variant)
    .value(Weight)
    .value(SubjectId)
    .value(EnvironmentName)
    .value(Base64String)
    .value(UserId)
    .value(EmailAddress)
    .value(TeamName)
    .value(PropertyKey)
    .value(Colour)
    .value(EnginePrincipal)
    .let { plugins.mapNotNull { p -> p.jsonExport }.fold(it) { builder, export -> export.mapping(builder)} }
    .done()
    .let { ConfigurableMoshi(it) }

// TODO see if this can be done without reflective annotations
private object BigDecimalAdapter {
    @Suppress("Unused") @FromJson fun fromJson(value: String) = BigDecimal(value)
    @Suppress("Unused") @ToJson fun toJson(value: BigDecimal?) = value?.toPlainString()
}

class JsonExport(
    val moshi: Moshi.Builder.() -> Moshi.Builder,
    val mapping: AutoMappingConfiguration<Moshi.Builder>.() -> AutoMappingConfiguration<Moshi.Builder> = { this }
) {
    operator fun plus(next: JsonExport) = JsonExport(
        moshi = { moshi(this); next.moshi(this) },
        mapping = { mapping(this); next.mapping(this) }
    )
}