package com.zalphion.featurecontrol.lib

import dev.andrewohara.utils.pagination.Paginator
import dev.andrewohara.utils.pagination.map

fun <Item: Any, NewItem: Any, Key: Any> Paginator<Item, Key>.mapItem(
    fn: (Item) -> NewItem
) = Paginator<NewItem, Key> { cursor ->
    this[cursor].map(fn)
}