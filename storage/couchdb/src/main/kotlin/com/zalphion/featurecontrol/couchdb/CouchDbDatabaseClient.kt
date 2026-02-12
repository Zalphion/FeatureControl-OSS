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

class CouchDbDatabaseClient(
    databaseUri: Uri, // e.g. http://localhost:5984/feature-flags
    credentials: Credentials, // the user must be an admin for the database given in the `databaseUri`
    internet: HttpHandler,
) {
    private val http = ClientFilters
        .SetBaseUriFrom(databaseUri)
        .then(ClientFilters.BasicAuth(credentials))
        .then(RequestFilters.SetHeader("Accept", ContentType.APPLICATION_JSON.value))
        .then(internet)

    fun createTable() {
        val response = Request(Method.PUT, "").let(http)
        // 409 is fine; just means the database already exists
        if (!response.status.successful && response.status != Status.PRECONDITION_FAILED) error(response)
    }

    fun getRev(id: String): String? = Request(Method.HEAD, id)
        .let(http)
        .takeIf { it.status.successful }
        ?.header("ETag")?.removeSurrounding("\"")

    fun createIndex(indexName: String, fields: List<String>) {
        val body = CouchDbCreateIndexRequest(
            index = mapOf("fields" to fields),
            name = indexName,
            type = "json"
        )

        Request(Method.POST, "_index")
            .with(CouchDbCreateIndexRequest.lens of body)
            .let(http)
            .also { if (!it.status.successful) error(it) }

    }

    fun save(document: CouchDbDocument) {
        Request(Method.PUT, document.id)
            .with(CouchDbDocument.lens of document)
            .let(http)
            .also { if (!it.status.successful) error(it) }
    }

    fun get(id: String): CouchDbDocument? {
        val response = Request(Method.GET, id)
            .let(http)

        return when(response.status) {
            Status.OK -> CouchDbDocument.lens(response)
            Status.NOT_FOUND -> null
            else -> error(response)
        }
    }

    fun batchGet(ids: List<String>): Map<String, CouchDbDocument?> {
        if (ids.isEmpty()) return emptyMap()

        return Request(Method.POST, "_all_docs")
            .query("include_docs", "true")
            .with(BulkDocsRequest.lens of BulkDocsRequest(ids))
            .let(http)
            .also { if (!it.status.successful) error(it) }
            .let(AllDocsResponse.lens)
            .rows.associate { it.key to it.doc }
    }

    fun listByPrefix(
        prefix: String,
        cursor: String?,
        limit: Int
    ): List<CouchDbDocument> {
        val startKey = if (cursor == null) prefix else "$prefix$cursor"

        // uses _all_docs instead of _find to take advantage of the existing key index
        return Request(Method.GET, "_all_docs")
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
        selector: Map<String, Any>,
        sort: List<Map<String, String>>,
        limit: Int
    ): List<CouchDbDocument> {
        val query = CouchDbQueryRequest(
            selector = selector,
            sort = sort,
            limit = limit
        )

        return Request(Method.POST, "_find")
            .with(CouchDbQueryRequest.lens of query)
            .let(http)
            .also { if (!it.status.successful) error(it) }
            .let(CouchDbQueryResponse.lens)
            .docs
    }

    fun delete(id: String) {
        val rev = getRev(id) ?: return // not found

        Request(Method.DELETE, id)
            .query("rev", rev)
            .let(http)
            .also { if (!it.status.successful) error(it) }
    }
}