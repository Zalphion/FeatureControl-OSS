@file:OptIn(ExperimentalKotshiApi::class)
package com.zalphion.featurecontrol.storage.couchdb

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.zalphion.featurecontrol.crypto.Base64String
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.asConfigurable
import org.http4k.format.value
import org.http4k.format.withStandardMappings
import se.ansman.kotshi.ExperimentalKotshiApi
import se.ansman.kotshi.JsonProperty
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
private object CouchDbAdminJsonAdapterFactory : JsonAdapter.Factory by KotshiCouchDbAdminJsonAdapterFactory

private val json = Moshi.Builder()
    .add(CouchDbAdminJsonAdapterFactory)
    .asConfigurable()
    .value(Base64String)
    .withStandardMappings()
    .done()
    .let(::ConfigurableMoshi)

@JsonSerializable
data class CouchDbSecurityGroup(
    val names: List<String> = emptyList(),
    val roles: List<String> = emptyList()
)

@JsonSerializable
data class CouchDbSecurity(
    val admins: CouchDbSecurityGroup = CouchDbSecurityGroup(),
    val members: CouchDbSecurityGroup = CouchDbSecurityGroup()
) {
    companion object {
        val lens = json.autoBody<CouchDbSecurity>().toLens()
    }
}

@JsonSerializable
data class CouchDbUser(
    val name: String,
    val password: String,
    val roles: List<String>,
    val type: String = "user",
    @JsonProperty("_id") val id: String = "org.couchdb.user:$name"
) {
    companion object {
        val lens = json.autoBody<CouchDbUser>().toLens()
    }
}
