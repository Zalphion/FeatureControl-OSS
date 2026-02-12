package com.zalphion.featurecontrol.storage.jdbc

import com.zalphion.featurecontrol.storage.Repository
import dev.andrewohara.utils.jdbc.toSequence
import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import org.http4k.lens.BiDiMapping
import javax.sql.DataSource
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.TABLE_NAME
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.COLLECTION_NAME
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.ITEM_ID_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.GROUP_ID_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.DOC_COLUMN

abstract class JdbcRepository<Doc: Any, GroupId: Any, ItemId: Any>(
    private val dataSource: DataSource,
    private val collectionName: String,
    private val documentMapper: BiDiMapping<String, Doc>,
    private val groupIdMapper: BiDiMapping<String, GroupId>,
    private val itemIdMapper: BiDiMapping<String, ItemId>,
    private val pageSize: Int
): Repository<Doc, GroupId, ItemId> {

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
                        .map { documentMapper(rs.getString(DOC_COLUMN)) }
                        .toList()
                }
            }
        }
    }

    override fun list(group: GroupId) = Paginator<Doc, ItemId> { cursor ->
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

    override fun listInverse(itemId: ItemId) = Paginator<Doc, GroupId> { cursor ->
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