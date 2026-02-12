package com.zalphion.featurecontrol.storage.mariadb

import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.jdbc.JdbcRepository
import com.zalphion.featurecontrol.storage.jdbc.migrate
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.TABLE_NAME
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.COLLECTION_NAME
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.ITEM_ID_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.GROUP_ID_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.DOC_COLUMN
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.Credentials
import org.http4k.core.Credentials as Http4kCredentials
import org.http4k.core.Uri
import org.http4k.lens.BiDiMapping
import javax.sql.DataSource

fun StorageDriver.Companion.mariaDb(
    uri: Uri,
    credentials: Http4kCredentials?,
    pageSize: PageSize,
) = object: StorageDriver {
    private val dataSource = HikariDataSource(HikariConfig().apply {
        this.jdbcUrl = uri.toString()
        this.credentials = credentials?.let { Credentials(it.user, it.password) }
    }).migrate()

    override fun <Doc : Any, GroupId : Any, ItemId : Any> create(
        collectionName: String,
        groupIdMapper: BiDiMapping<String, GroupId>,
        itemIdMapper: BiDiMapping<String, ItemId>,
        documentMapper: BiDiMapping<String, Doc>
    ) = mariaDbRepository(
        dataSource = dataSource,
        collectionName = collectionName,
        documentMapper = documentMapper,
        groupIdMapper = groupIdMapper,
        itemIdMapper = itemIdMapper,
        pageSize = pageSize.value
    )
}

private fun <Doc: Any, GroupId: Any, ItemId: Any> mariaDbRepository(
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
                INSERT INTO $TABLE_NAME
                    ($COLLECTION_NAME, $GROUP_ID_COLUMN, $ITEM_ID_COLUMN, $DOC_COLUMN)
                    VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    $DOC_COLUMN = VALUES($DOC_COLUMN)
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