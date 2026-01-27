package com.zalphion.featurecontrol.lib

import dev.andrewohara.utils.pagination.Paginator
import dev.andrewohara.utils.pagination.filter
import dev.andrewohara.utils.pagination.map

fun <Item: Any, NewItem: Any, Key: Any> Paginator<Item, Key>.mapItem(
    fn: (Item) -> NewItem
) = Paginator<NewItem, Key> { cursor ->
    this[cursor].map(fn)
}

fun <Item: Any, Key: Any> Paginator<Item, Key>.filterItem(
    fn: (Item) -> Boolean
) = Paginator<Item, Key> { cursor ->
    this[cursor].filter(fn)
}