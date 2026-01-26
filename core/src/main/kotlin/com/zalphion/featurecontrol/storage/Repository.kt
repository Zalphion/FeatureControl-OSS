package com.zalphion.featurecontrol.storage

import dev.andrewohara.utils.pagination.Paginator

interface Repository<Doc: Any, GroupId: Any, ItemId: Any> {
    fun save(groupId: GroupId, itemId: ItemId, doc: Doc)

    fun delete(groupId: GroupId, itemId: ItemId)

    operator fun get(groupId: GroupId, itemId: ItemId): Doc?
    operator fun get(ids: Collection<Pair<GroupId, ItemId>>): Collection<Doc>

    fun list(group: GroupId): Paginator<Doc, ItemId>
    fun listInverse(itemId: ItemId): Paginator<Doc, GroupId>
}