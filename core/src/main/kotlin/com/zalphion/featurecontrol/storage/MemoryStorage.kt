package com.zalphion.featurecontrol.storage

import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import org.http4k.lens.BiDiMapping

fun Storage.Companion.memory() = object: Storage {

    private val files = mutableMapOf<String, MutableMap<Pair<String, String>, String>>()

    override fun <Doc : Any, GroupId : Any, ItemId : Any> create(
        name: String,
        groupIdMapper: BiDiMapping<String, GroupId>,
        itemIdMapper: BiDiMapping<String, ItemId>,
        documentMapper: BiDiMapping<String, Doc>
    ) = memoryRepository(
        files = files.getOrPut(name) { mutableMapOf() },
        groupIdMapper = groupIdMapper,
        itemIdMapper = itemIdMapper,
        documentMapper = documentMapper
    )
}

private fun <Doc: Any, GroupId: Any, ItemId: Any> memoryRepository(
    files: MutableMap<Pair<String, String>, String>,
    groupIdMapper: BiDiMapping<String, GroupId>,
    itemIdMapper: BiDiMapping<String, ItemId>,
    documentMapper: BiDiMapping<String, Doc>,
) = object: Repository<Doc, GroupId, ItemId> {

    override fun save(groupId: GroupId, itemId: ItemId, doc: Doc) {
        files[groupIdMapper(groupId) to itemIdMapper(itemId)] = documentMapper(doc)
    }

    override fun delete(groupId: GroupId, itemId: ItemId) {
        files.remove(groupIdMapper(groupId) to itemIdMapper(itemId))
    }

    override fun get(groupId: GroupId, itemId: ItemId): Doc? {
        return files[groupIdMapper(groupId) to itemIdMapper(itemId)]
            ?.let(documentMapper::invoke)
    }

    override fun get(ids: Collection<Pair<GroupId, ItemId>>) = ids
        .mapNotNull { (groupId, itemId) -> get(groupId, itemId) }

    override fun list(group: GroupId, pageSize: Int) = Paginator<Doc, ItemId> { cursor ->
        val results = files
            .filterKeys { it.first == groupIdMapper(group) }
            .entries
            .sortedBy { it.key.second }
            .dropWhile { cursor != null && it.key.second <= itemIdMapper(cursor) }
            .take(pageSize + 1)

        Page(
            items = results.take(pageSize).map { documentMapper(it.value) },
            next = results
                .takeIf { it.size > pageSize }?.get(pageSize - 1)
                ?.key?.second
                ?.let(itemIdMapper::invoke)
        )
    }

    override fun listInverse(itemId: ItemId, pageSize: Int) = Paginator<Doc, GroupId> { cursor ->
        val results = files
            .filterKeys { it.second == itemIdMapper(itemId) }
            .entries
            .sortedBy { it.key.first }
            .dropWhile { cursor != null && it.key.first <= groupIdMapper(cursor) }
            .take(pageSize + 1)

        Page(
            items = results.take(pageSize).map { documentMapper(it.value) },
            next = results
                .takeIf { it.size > pageSize }?.get(pageSize - 1)
                ?.key?.first
                ?.let(groupIdMapper::invoke)
        )
    }
}