package com.zalphion.featurecontrol.storage.embedded

import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.jdbc.JdbcRepository
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.TABLE_NAME
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.COLLECTION_NAME
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.ITEM_ID_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.GROUP_ID_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.DOC_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.migrate
import org.h2.jdbcx.JdbcConnectionPool
import org.http4k.core.Uri
import org.http4k.lens.BiDiMapping
import java.nio.file.Path
import java.util.UUID
import javax.sql.DataSource
import kotlin.use

/**
 * Uses an embedded H2 database with a volatile in-memory store.
 *
 * Suitable for testing, but not production use.
 */
fun StorageDriver.Companion.embeddedMemory(pageSize: PageSize) = driver(
    url = Uri.of("jdbc:h2:mem:${UUID.randomUUID()}").query("DB_CLOSE_DELAY=-1"),
    pageSize = pageSize
)

/**
 * Uses an embedded H2 database with filesystem persistence.
 *
 * WARNING: Only a single storage driver is supported per file.
 * Multi-replica deployments are unsuitable for this driver.
 */
fun StorageDriver.Companion.embeddedSingleNode(file: Path, pageSize: PageSize) = driver(
    url = Uri.of("jdbc:h2:$file"),
    pageSize = pageSize
)

private fun driver(url: Uri, pageSize: PageSize) = object: StorageDriver {

    private val dataSource = JdbcConnectionPool
        .create(url.toString(), "sa", "")
        .migrate()

    override fun <Doc : Any, GroupId : Any, ItemId : Any> create(
        collectionName: String,
        groupIdMapper: BiDiMapping<String, GroupId>,
        itemIdMapper: BiDiMapping<String, ItemId>,
        documentMapper: BiDiMapping<String, Doc>
    ) = embeddedRepository(
        dataSource = dataSource,
        collectionName = collectionName,
        documentMapper = documentMapper,
        groupIdMapper = groupIdMapper,
        itemIdMapper = itemIdMapper,
        pageSize = pageSize.value
    )
}

private fun <Doc: Any, GroupId: Any, ItemId: Any> embeddedRepository(
    dataSource: DataSource,
    collectionName: String,
    documentMapper: BiDiMapping<String, Doc>,
    groupIdMapper: BiDiMapping<String, GroupId>,
    itemIdMapper: BiDiMapping<String, ItemId>,
    pageSize: Int
) = object: JdbcRepository<Doc, GroupId, ItemId>(
    dataSource = dataSource,
    collectionName = collectionName,
    documentMapper = documentMapper,
    groupIdMapper = groupIdMapper,
    itemIdMapper = itemIdMapper,
    pageSize = pageSize
) {

    override fun save(groupId: GroupId, itemId: ItemId, doc: Doc) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                MERGE INTO $TABLE_NAME
                    ($COLLECTION_NAME, $GROUP_ID_COLUMN, $ITEM_ID_COLUMN, $DOC_COLUMN)
                    KEY ($COLLECTION_NAME, $GROUP_ID_COLUMN, $ITEM_ID_COLUMN)
                    VALUES (?, ?, ?, ?)
            """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, collectionName)
                stmt.setString(2, groupIdMapper(groupId))
                stmt.setString(3, itemIdMapper(itemId))
                stmt.setString(4, documentMapper(doc))

                stmt.execute()
            }
        }
    }
}