package dev.jovanni0.database

import dev.jovanni0.database.transactions.sharedTransaction
import dev.jovanni0.tables.User
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.select
import javax.sql.DataSource



fun migrateTenantSchemas(data_source: DataSource, schema_name: String)
{
    Flyway
        .configure()
        .dataSource(data_source)
        .schemas(schema_name)
        .locations("classpath:db/migration/tenant")
        .load()
        .migrate()
}


fun migrateAllTenantSchemas(data_source: DataSource)
{
    val schema_names = sharedTransaction {
        User
            .select(User.id)
            .map { "user_${it[User.id]}" }
    }

    for (schema_name in schema_names)
    {
        migrateTenantSchemas(data_source, schema_name)
    }
}