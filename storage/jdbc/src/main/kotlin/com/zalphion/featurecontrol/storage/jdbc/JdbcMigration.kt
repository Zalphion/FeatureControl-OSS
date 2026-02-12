package com.zalphion.featurecontrol.storage.jdbc

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.migration.Context
import org.flywaydb.core.api.migration.JavaMigration
import javax.sql.DataSource
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.TABLE_NAME
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.COLLECTION_NAME
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.ITEM_ID_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.GROUP_ID_COLUMN
import com.zalphion.featurecontrol.storage.jdbc.JdbcNames.DOC_COLUMN

fun <T: DataSource> T.migrate(): T {
    Flyway.configure()
        .dataSource(this@migrate)
        .javaMigrations(V1CreateMonoTable())
        .createSchemas(true)
        .load()
        .migrate()
    return this
}

private class V1CreateMonoTable: JavaMigration {
    override fun getVersion(): MigrationVersion = MigrationVersion.fromVersion("1")
    override fun getDescription() = "Create mono table"
    override fun getChecksum() = (createTable.hashCode() * 3) + createIndex.hashCode()
    override fun canExecuteInTransaction() = true

    private val createTable =
"""CREATE TABLE $TABLE_NAME (
    $COLLECTION_NAME VARCHAR(256) NOT NULL,
    $GROUP_ID_COLUMN VARCHAR(256) NOT NULL,
    $ITEM_ID_COLUMN VARCHAR(256) NOT NULL,
    $DOC_COLUMN TEXT NOT NULL,
    PRIMARY KEY ($COLLECTION_NAME, $GROUP_ID_COLUMN, $ITEM_ID_COLUMN)
);"""

private val createIndex = "CREATE INDEX idx_inverse ON $TABLE_NAME ($COLLECTION_NAME, $ITEM_ID_COLUMN, $GROUP_ID_COLUMN);"

    override fun migrate(context: Context) {
        context.connection.createStatement().use { stmt ->
            stmt.execute(createTable)
            stmt.execute(createIndex)
        }
    }
}