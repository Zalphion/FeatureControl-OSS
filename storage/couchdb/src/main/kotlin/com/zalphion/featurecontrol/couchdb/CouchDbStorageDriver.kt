package com.zalphion.featurecontrol.couchdb

import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.StorageDriver
import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import org.http4k.client.JavaHttpClient
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.lens.BiDiMapping

private const val KEY_DELIMITER = "-"

fun StorageDriver.Companion.couchDb(
    databaseUri: Uri, // must include an EXISTING database eg http://couchdb-host:5984/db-name
    credentials: Credentials, //  must be for a database admin for the given `databaseUri`
    internet: HttpHandler = JavaHttpClient(),
    pageSize: PageSize = PageSize.of(100)
) = object: StorageDriver {

    private val client = CouchDbDatabaseClient(databaseUri, credentials, internet).apply {
        // always ensure the index is up to date
        createIndex(
            indexName = "inverse",
            fields = listOf(CouchDbDocument.COLLECTION_PROP, CouchDbDocument.ITEM_ID_PROP, CouchDbDocument.GROUP_ID_PROP)
        )
    }

    override fun <Doc : Any, GroupId : Any, ItemId : Any> create(
        collectionName: String,
        groupIdMapper: BiDiMapping<String, GroupId>,
        itemIdMapper: BiDiMapping<String, ItemId>,
        documentMapper: BiDiMapping<String, Doc>
    ) = repository(
        client = client,
        collectionName = collectionName,
        groupIdMapper = groupIdMapper,
        itemIdMapper = itemIdMapper,
        documentMapper = documentMapper,
        pageSize = pageSize
    )
}

private fun <Doc : Any, GroupId : Any, ItemId : Any> repository(
    client: CouchDbDatabaseClient,
    collectionName: String,
    groupIdMapper: BiDiMapping<String, GroupId>,
    itemIdMapper: BiDiMapping<String, ItemId>,
    documentMapper: BiDiMapping<String, Doc>,
    pageSize: PageSize
) = object: Repository<Doc, GroupId, ItemId> {

    private fun CouchDbDocument.document() = documentMapper(documentJson)

    fun key(groupId: GroupId, itemId: ItemId?): String {
        val itemIdStr = itemId?.let(itemIdMapper::invoke) ?: ""
        return "$collectionName$KEY_DELIMITER${groupIdMapper(groupId)}$KEY_DELIMITER$itemIdStr"
    }

    override fun save(groupId: GroupId, itemId: ItemId, doc: Doc) {
        val id = key(groupId, itemId)
        val envelope = CouchDbDocument(
            id = id,
            rev = client.getRev(id),
            collection = collectionName,
            groupId = groupIdMapper(groupId),
            itemId = itemIdMapper(itemId),
            documentJson = documentMapper(doc)
        )

        client.save(envelope)
    }

    override fun delete(groupId: GroupId, itemId: ItemId) {
        val id = key(groupId, itemId)
        client.delete(id)
    }

    override fun get(groupId: GroupId, itemId: ItemId): Doc? = client
        .get(key(groupId, itemId))
        ?.document()

    override fun get(ids: Collection<Pair<GroupId, ItemId>>) = client
        .batchGet(ids.map { key(it.first, it.second) })
        .values.mapNotNull { it?.document() }

    private fun <T> List<CouchDbDocument>.toPage(cursorFn: (CouchDbDocument) -> T) = Page(
        items = take(pageSize.value).map { it.document() },
        next = if (size > pageSize.value) {
            cursorFn(get(pageSize.value - 1))
        } else {
            null
        }
    )

    override fun list(group: GroupId) = Paginator<Doc, ItemId> { cursor ->
        client.listByPrefix(
            prefix = key(group, null),
            cursor = cursor?.let(itemIdMapper::invoke),
            limit = pageSize.value.plus(1)
        ).toPage { itemIdMapper(it.itemId) }
    }

    override fun listInverse(itemId: ItemId) = Paginator<Doc, GroupId> { cursor ->
        client.find(
            selector = buildMap {
                put(CouchDbDocument.COLLECTION_PROP, collectionName)
                put(CouchDbDocument.ITEM_ID_PROP, itemIdMapper(itemId))
                if (cursor != null) {
                    this[CouchDbDocument.GROUP_ID_PROP] = mapOf(
                        $$"$gt" to groupIdMapper(cursor)
                    )
                }
            },
            sort = listOf(mapOf(CouchDbDocument.GROUP_ID_PROP to "asc")),
            limit = pageSize.value + 1
        ).toPage { groupIdMapper(it.groupId) }
    }
}