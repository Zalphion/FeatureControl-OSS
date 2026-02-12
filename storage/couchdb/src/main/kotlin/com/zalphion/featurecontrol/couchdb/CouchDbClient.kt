package com.zalphion.featurecontrol.couchdb

import org.http4k.core.ContentType
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ClientFilters
import org.http4k.filter.RequestFilters

class CouchDbClient(internet: HttpHandler, host: Uri, credentials: Credentials) {

    private val http = ClientFilters
        .SetHostFrom(host)
        .then(ClientFilters.BasicAuth(credentials))
        .then(RequestFilters.SetHeader("Accept", ContentType.APPLICATION_JSON.value))
        .then(internet)

    fun createTable(databaseName: String) {
        val response = Request(Method.PUT, databaseName).let(http)
        // 409 is fine; just means the database already exists
        if (!response.status.successful || response.status == Status.PRECONDITION_FAILED) error(response)
    }

    fun getRev(databaseName: String, id: String): String? = Request(Method.HEAD, "$databaseName/$id")
        .let(http)
        .takeIf { it.status.successful }
        ?.header("ETag")?.removeSurrounding("\"")

    fun createIndex(databaseName: String, indexName: String, fields: List<String>) {
        val body = CouchDbCreateIndexRequest(
            index = mapOf("fields" to fields),
            name = indexName,
            type = "json"
        )

        Request(Method.POST, "$databaseName/_index")
            .with(CouchDbCreateIndexRequest.lens of body)
            .let(http)
            .also { if (!it.status.successful) error(it) }

    }

    fun save(databaseName: String, document: CouchDbDocument) {
        Request(Method.PUT, "$databaseName/${document.id}")
            .with(CouchDbDocument.lens of document)
            .let(http)
            .also { if (!it.status.successful) error(it) }
    }

    fun get(databaseName: String, id: String): CouchDbDocument? {
        val response = Request(Method.GET, "$databaseName/$id")
            .let(http)

        return when(response.status) {
            Status.OK -> CouchDbDocument.lens(response)
            Status.NOT_FOUND -> null
            else -> error(response)
        }
    }

    fun batchGet(databaseName: String, ids: List<String>): Map<String, CouchDbDocument?> {
        if (ids.isEmpty()) return emptyMap()

        return Request(Method.POST, "$databaseName/_all_docs")
            .query("include_docs", "true")
            .with(BulkDocsRequest.lens of BulkDocsRequest(ids))
            .let(http)
            .also { if (!it.status.successful) error(it) }
            .let(AllDocsResponse.lens)
            .rows.associate { it.key to it.doc }
    }

    fun listByPrefix(
        databaseName: String,
        prefix: String,
        cursor: String?,
        limit: Int
    ): List<CouchDbDocument> {
        val startKey = if (cursor == null) prefix else "$prefix$cursor"

        // uses _all_docs instead of _find to take advantage of the existing key index
        return Request(Method.GET, "$databaseName/_all_docs")
            .query("include_docs", "true")
            .query("startkey", "\"$startKey\"")
            .query("endkey", "\"$prefix\\ufff0\"")
            .query("limit", limit.plus(1).toString())
            // If we have a cursor, skip the first item as it's the one we've already seen
            .let { if (cursor != null) it.query("skip", "1") else it }
            .let(http)
            .also { if (!it.status.successful) error(it) }
            .let(AllDocsResponse.lens)
            .rows.mapNotNull { it.doc }
    }

    fun find(
        databaseName: String,
        selector: Map<String, Any>,
        sort: List<Map<String, String>>,
        limit: Int
    ): List<CouchDbDocument> {
        val query = CouchDbQueryRequest(
            selector = selector,
            sort = sort,
            limit = limit
        )

        return Request(Method.POST, "$databaseName/_find")
            .with(CouchDbQueryRequest.lens of query)
            .let(http)
            .also { if (!it.status.successful) error(it) }
            .let(CouchDbQueryResponse.lens)
            .docs
    }

    fun delete(databaseName: String, id: String) {
        val rev = getRev(databaseName, id) ?: return // not found

        Request(Method.DELETE, "$databaseName/$id")
            .query("rev", rev)
            .let(http)
            .also { if (!it.status.successful) error(it) }
    }
}