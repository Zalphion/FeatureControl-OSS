package com.zalphion.featurecontrol.storage

import org.http4k.lens.BiDiMapping

interface Storage {
    fun <Doc: Any, GroupId: Any, ItemId: Any> create(
        name: String,
        groupIdMapper: BiDiMapping<String, GroupId>,
        itemIdMapper: BiDiMapping<String, ItemId>,
        documentMapper: BiDiMapping<String, Doc>
    ): Repository<Doc, GroupId, ItemId>

    companion object
}