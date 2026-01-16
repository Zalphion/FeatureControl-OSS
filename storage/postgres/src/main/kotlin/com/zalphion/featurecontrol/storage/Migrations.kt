package com.zalphion.featurecontrol.storage

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.migration.Context
import org.flywaydb.core.api.migration.JavaMigration

internal fun HikariDataSource.migrate(): HikariDataSource {
    Flyway
        .configure()
        .dataSource(this)
        .schemas(this.schema)
        .createSchemas(true)
        .javaMigrations(V1CreateMonoTable())
        .load()
        .migrate()
    return this
}

private class V1CreateMonoTable: JavaMigration {
    override fun getVersion(): MigrationVersion = MigrationVersion.fromVersion("1")
    override fun getDescription() = "Create mono table"
    override fun getChecksum() = sql.hashCode()
    override fun canExecuteInTransaction() = true

    private val sql = """CREATE TABLE $TABLE_NAME (
    $COLLECTION_NAME VARCHAR(256) NOT NULL,
    $GROUP_ID_COLUMN VARCHAR(256) NOT NULL,
    $ITEM_ID_COLUMN VARCHAR(256) NOT NULL,
    $DOC_COLUMN TEXT NOT NULL,
    PRIMARY KEY ($COLLECTION_NAME, $GROUP_ID_COLUMN, $ITEM_ID_COLUMN)
);

CREATE INDEX idx_inverse
    ON $TABLE_NAME ($COLLECTION_NAME, $ITEM_ID_COLUMN, $GROUP_ID_COLUMN);"""

    override fun migrate(context: Context) {
        context.connection.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }
}