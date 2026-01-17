package com.zalphion.featurecontrol.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.Credentials
import org.http4k.core.Credentials as Http4kCredentials
import dev.andrewohara.utils.jdbc.toSequence
import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import org.http4k.core.Uri
import org.http4k.lens.BiDiMapping
import javax.sql.DataSource
import kotlin.collections.take

internal const val TABLE_NAME = "documents"
internal const val COLLECTION_NAME = "collection_name"
internal const val GROUP_ID_COLUMN = "group_id"
internal const val ITEM_ID_COLUMN = "item_id"
internal const val DOC_COLUMN = "document"

fun Storage.Companion.postgres(uri: Uri, credentials: Http4kCredentials?) = object: Storage {
    private val dataSource = HikariDataSource(HikariConfig().apply {
        this.jdbcUrl = uri.toString()
        this.credentials = credentials?.let { Credentials(it.user, it.password) }
    }).migrate()

    override fun <Doc : Any, GroupId : Any, ItemId : Any> create(
        name: String,
        groupIdMapper: BiDiMapping<String, GroupId>,
        itemIdMapper: BiDiMapping<String, ItemId>,
        documentMapper: BiDiMapping<String, Doc>
    ) = postgresRepository(
        dataSource = dataSource,
        collectionName = name,
        documentMapper = documentMapper,
        groupIdMapper = groupIdMapper,
        itemIdMapper = itemIdMapper
    )
}

private fun <Doc: Any, GroupId: Any, ItemId: Any> postgresRepository(
    dataSource: DataSource,
    collectionName: String,
    documentMapper: BiDiMapping<String, Doc>,
    groupIdMapper: BiDiMapping<String, GroupId>,
    itemIdMapper: BiDiMapping<String, ItemId>,
) = object: Repository<Doc, GroupId, ItemId> {

    override fun save(groupId: GroupId, itemId: ItemId, doc: Doc) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO $TABLE_NAME
                    ($COLLECTION_NAME, $GROUP_ID_COLUMN, $ITEM_ID_COLUMN, $DOC_COLUMN)
                    VALUES (?, ?, ?, ?)
                ON CONFLICT ($COLLECTION_NAME, $GROUP_ID_COLUMN, $ITEM_ID_COLUMN)
                DO UPDATE SET $DOC_COLUMN = EXCLUDED.$DOC_COLUMN;
            """
            ).use { stmt ->
                stmt.setString(1, collectionName)
                stmt.setString(2, groupIdMapper(groupId))
                stmt.setString(3, itemIdMapper(itemId))
                stmt.setString(4, documentMapper(doc))

                stmt.execute()
            }
        }
    }

    override fun delete(groupId: GroupId, itemId: ItemId) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                DELETE FROM $TABLE_NAME
                WHERE
                    $COLLECTION_NAME = ?
                    AND $GROUP_ID_COLUMN = ?
                    AND $ITEM_ID_COLUMN = ?
            """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, collectionName)
                stmt.setString(2, groupIdMapper(groupId))
                stmt.setString(3, itemIdMapper(itemId))

                stmt.execute()
            }
        }
    }

    override fun get(groupId: GroupId, itemId: ItemId) = dataSource.connection.use { conn ->
        conn.prepareStatement("""
            SELECT *
            FROM $TABLE_NAME
            WHERE
                $COLLECTION_NAME = ?
                AND $GROUP_ID_COLUMN = ?
                AND $ITEM_ID_COLUMN = ?
        """.trimIndent()).use { stmt ->
            stmt.setString(1, collectionName)
            stmt.setString(2, groupIdMapper(groupId))
            stmt.setString(3, itemIdMapper(itemId))

            stmt.executeQuery().use { rs ->
                if (!rs.next()) null else documentMapper(rs.getString(DOC_COLUMN))
            }
        }
    }

    override fun get(ids: Collection<Pair<GroupId, ItemId>>): Collection<Doc> {
        if (ids.isEmpty()) return emptyList()

        return dataSource.connection.use { conn ->
            conn.prepareStatement("""
                SELECT *
                FROM $TABLE_NAME
                WHERE
                    $COLLECTION_NAME = ?
                    AND ($GROUP_ID_COLUMN, $ITEM_ID_COLUMN) IN (${ids.joinToString(",") { "(?, ?)" }})
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, collectionName)
                ids.forEachIndexed { index, (groupId, itemId) ->
                    stmt.setString(index * 2 + 2, groupIdMapper(groupId))
                    stmt.setString(index * 2 + 3, itemIdMapper(itemId))
                }

                stmt.executeQuery().use { rs ->
                    rs.toSequence()
                        .map {
                            println(rs.getString(DOC_COLUMN))
                            documentMapper(rs.getString(DOC_COLUMN))
                        }
                        .toList()
                }
            }
        }
    }

    override fun list(group: GroupId, pageSize: Int) = Paginator<Doc, ItemId> { cursor ->
        val result = dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                    SELECT *
                    FROM $TABLE_NAME
                    WHERE
                        $COLLECTION_NAME = ?
                        AND $GROUP_ID_COLUMN = ?
                        AND $ITEM_ID_COLUMN > ?
                    ORDER BY $ITEM_ID_COLUMN ASC
                    LIMIT ?;
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, collectionName)
                stmt.setString(2, groupIdMapper(group))
                stmt.setString(3, cursor?.let(itemIdMapper::invoke) ?: "0")
                stmt.setInt(4, pageSize + 1)

                stmt.executeQuery().use { rs ->
                    rs.toSequence()
                        .map {
                            val itemId = itemIdMapper(rs.getString(ITEM_ID_COLUMN))
                            val doc = documentMapper(rs.getString(DOC_COLUMN))
                            itemId to doc
                        }
                        .toList()
                }
            }
        }

        Page(
            items = result.take(pageSize).map { it.second },
            next = result.takeIf { it.size > pageSize }?.get(pageSize - 1)?.first
        )
    }

    override fun listInverse(itemId: ItemId, pageSize: Int) = Paginator<Doc, GroupId> { cursor ->
        val result = dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                    SELECT *
                    FROM $TABLE_NAME
                    WHERE
                        $COLLECTION_NAME = ?
                        AND $ITEM_ID_COLUMN = ?
                        AND $GROUP_ID_COLUMN > ?
                    ORDER BY $GROUP_ID_COLUMN ASC
                    LIMIT ?;
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, collectionName)
                stmt.setString(2, itemIdMapper(itemId))
                stmt.setString(3, cursor?.let(groupIdMapper::invoke) ?: "0")
                stmt.setInt(4, pageSize + 1)

                stmt.executeQuery().use { rs ->
                    rs.toSequence()
                        .map {
                            val groupId = groupIdMapper(rs.getString(GROUP_ID_COLUMN))
                            val doc = documentMapper(rs.getString(DOC_COLUMN))
                            groupId to doc
                        }
                        .toList()
                }
            }
        }

        Page(
            items = result.take(pageSize).map { it.second },
            next = result.takeIf { it.size > pageSize }?.get(pageSize - 1)?.first
        )
    }
}