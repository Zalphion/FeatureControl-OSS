package com.zalphion.featurecontrol.storage

import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import org.http4k.lens.BiDiMapping
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

fun StorageDriver.Companion.filesystem(root: Path) = object: StorageDriver {

    init {
        Files.createDirectories(root)
    }

    override fun <Doc : Any, GroupId : Any, ItemId : Any> create(
        name: String,
        groupIdMapper: BiDiMapping<String, GroupId>,
        itemIdMapper: BiDiMapping<String, ItemId>,
        documentMapper: BiDiMapping<String, Doc>
    ) = filesystemRepository(
        dir = root.resolve(name).also(Files::createDirectory),
        groupIdMapper = groupIdMapper,
        itemIdMapper = itemIdMapper,
        documentMapper = documentMapper
    )
}

private class FileMatch<GroupId: Any, ItemId: Any>(
    val groupId: GroupId,
    val itemId: ItemId,
    private val path: Path
): Comparable<FileMatch<GroupId, ItemId>> {
    fun <Doc: Any> read(mapper: BiDiMapping<String, Doc>) = mapper(Files.readString(path))

    override fun compareTo(other: FileMatch<GroupId, ItemId>) = path.compareTo(other.path)
}

private fun <Doc: Any, GroupId: Any, ItemId: Any> filesystemRepository(
    dir: Path,
    groupIdMapper: BiDiMapping<String, GroupId>,
    itemIdMapper: BiDiMapping<String, ItemId>,
    documentMapper: BiDiMapping<String, Doc>
) = object: Repository<Doc, GroupId, ItemId> {

    private fun Path.toMatch(): FileMatch<GroupId, ItemId>? {
        val parts = nameWithoutExtension.removeSuffix(".json").split('.')
        if (parts.size != 2) return null
        return FileMatch(groupIdMapper(parts[0]), itemIdMapper(parts[1]), this)
    }

    private fun Path.file(groupId: GroupId, itemId: ItemId) = this
        .resolve("${groupIdMapper(groupId)}.${itemIdMapper(itemId)}.json")

    override fun save(groupId: GroupId, itemId: ItemId, doc: Doc) {
        Files.writeString(dir.file(groupId, itemId), documentMapper(doc))
    }

    override fun delete(groupId: GroupId, itemId: ItemId) {
        Files.deleteIfExists(dir.file(groupId, itemId))
    }

    override fun get(groupId: GroupId, itemId: ItemId) = dir
        .file(groupId, itemId)
        .takeIf(Files::exists)
        ?.let { Files.readString(it) }
        ?.let(documentMapper::invoke)

    override fun get(ids: Collection<Pair<GroupId, ItemId>>) = ids
        .mapNotNull { (groupId, itemId) -> get(groupId, itemId) }

    override fun list(group: GroupId, pageSize: Int): Paginator<Doc, ItemId> {
        val matches = Files
            .newDirectoryStream(dir, "${groupIdMapper(group)}.*.json")
            .use { stream -> stream.mapNotNull { it.toMatch() }.sorted().toList() }

        return Paginator { cursor ->
            val cursorString = cursor?.let(itemIdMapper::invoke)
            val results = matches
                .dropWhile { cursorString != null && itemIdMapper(it.itemId) <= cursorString }
                .take(pageSize + 1)

            Page(
                items = results.take(pageSize).map { it.read(documentMapper) },
                next = results.takeIf { it.size > pageSize }?.get(pageSize - 1)?.itemId
            )
        }
    }

    override fun listInverse(itemId: ItemId, pageSize: Int): Paginator<Doc, GroupId> {
        val matches = Files
            .newDirectoryStream(dir, "*.${itemIdMapper(itemId)}.json")
            .use { stream -> stream.mapNotNull { it.toMatch() }.sorted().toList() }

        return Paginator { cursor ->
            val cursorString = cursor?.let(groupIdMapper::invoke)
            val results = matches
                .dropWhile { cursorString != null && groupIdMapper(it.groupId) <= cursorString }
                .take(pageSize + 1)

            Page(
                items = results.take(pageSize).map { it.read(documentMapper) },
                next = results.takeIf { it.size > pageSize }?.get(pageSize - 1)?.groupId
            )
        }
    }
}