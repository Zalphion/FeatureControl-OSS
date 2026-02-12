@file:OptIn(ExperimentalKotshiApi::class)
package com.zalphion.featurecontrol.couchdb

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
private object CouchDbJsonAdapterFactory : JsonAdapter.Factory by KotshiCouchDbJsonAdapterFactory

private val json = Moshi.Builder()
    .add(CouchDbJsonAdapterFactory)
    .asConfigurable()
    .value(Base64String)
    .withStandardMappings()
    .done()
    .let(::ConfigurableMoshi)

@JsonSerializable
data class CouchDbDocument(
    @JsonProperty("_id") val id: String,
    @JsonProperty("_rev") val rev: String? = null,
    val collection: String,
    val groupId: String,
    val itemId: String,
    val documentJson: String
) {
    companion object {
        const val COLLECTION_PROP = "collection"
        const val GROUP_ID_PROP = "groupId"
        const val ITEM_ID_PROP = "itemId"
        val lens = json.autoBody<CouchDbDocument>().toLens()
    }
}

@JsonSerializable
data class BulkDocsRequest(val keys: List<String>) {
    companion object {
        val lens = json.autoBody<BulkDocsRequest>().toLens()
    }
}

@JsonSerializable
data class AllDocsResponse(val rows: List<AllDocsRow>) {
    companion object {
        val lens = json.autoBody<AllDocsResponse>().toLens()
    }
}

@JsonSerializable
data class AllDocsRow(val key: String, val doc: CouchDbDocument?, val error: String?)

@JsonSerializable
data class CouchDbQueryRequest(
    val selector: Map<String, Any>,
    val sort: List<Map<String, String>>? = null,
    val limit: Int? = null,
    val skip: Int? = null
) {
    companion object {
        val lens = json.autoBody<CouchDbQueryRequest>().toLens()
    }
}

@JsonSerializable
data class CouchDbCreateIndexRequest(
    val index: Map<String, List<String>>,
    val name: String,
    val type: String
) {
    companion object {
        val lens = json.autoBody<CouchDbCreateIndexRequest>().toLens()
    }
}

@JsonSerializable
data class CouchDbQueryResponse(
    val docs: List<CouchDbDocument>,
    val bookmark: String?
) {
    companion object {
        val lens = json.autoBody<CouchDbQueryResponse>().toLens()
    }
}

