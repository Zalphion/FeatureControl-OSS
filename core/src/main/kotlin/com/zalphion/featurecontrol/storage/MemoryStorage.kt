package com.zalphion.featurecontrol.storage

import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import org.http4k.lens.BiDiMapping

fun Storage.Companion.memory() = object: Storage {

    override fun <Doc : Any, GroupId : Any, ItemId : Any> create(
        name: String,
        groupIdMapper: BiDiMapping<String, GroupId>,
        itemIdMapper: BiDiMapping<String, ItemId>,
        documentMapper: BiDiMapping<String, Doc>
    ) = memoryRepository<Doc, GroupId, ItemId>(groupIdMapper, itemIdMapper)
}

private fun <Doc: Any, GroupId: Any, ItemId: Any> memoryRepository(
    groupIdMapper: BiDiMapping<String, GroupId>,
    itemIdMapper: BiDiMapping<String, ItemId>,
) = object: Repository<Doc, GroupId, ItemId> {

    private val files = mutableMapOf<Pair<GroupId, ItemId>, Doc>()

    override fun save(groupId: GroupId, itemId: ItemId, doc: Doc) {
        files[groupId to itemId] = doc
    }

    override fun delete(groupId: GroupId, itemId: ItemId) {
        files.remove(groupId to itemId)
    }

    override fun get(groupId: GroupId, itemId: ItemId): Doc? {
        return files[groupId to itemId]
    }

    override fun get(ids: Collection<Pair<GroupId, ItemId>>) = ids
        .mapNotNull { (groupId, itemId) -> get(groupId, itemId) }

    override fun list(group: GroupId, pageSize: Int) = Paginator<Doc, ItemId> { cursor ->
        val results = files
            .filterKeys { it.first == group }
            .entries
            .sortedBy { itemIdMapper(it.key.second) }
            .dropWhile { cursor != null && itemIdMapper(it.key.second) <= itemIdMapper(cursor) }
            .take(pageSize + 1)

        Page(
            items = results.take(pageSize).map { it.value },
            next = results.takeIf { it.size > pageSize }?.get(pageSize - 1)?.key?.second
        )
    }

    override fun listInverse(itemId: ItemId, pageSize: Int) = Paginator<Doc, GroupId> { cursor ->
        val results = files
            .filterKeys { it.second == itemId }
            .entries
            .sortedBy { groupIdMapper(it.key.first) }
            .dropWhile { cursor != null && groupIdMapper(it.key.first) <= groupIdMapper(cursor) }
            .take(pageSize + 1)

        Page(
            items = results.take(pageSize).map { it.value },
            next = results.takeIf { it.size > pageSize }?.get(pageSize - 1)?.key?.first
        )
    }
}