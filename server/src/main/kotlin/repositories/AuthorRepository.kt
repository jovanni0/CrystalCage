package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.author.AuthorRow
import dev.jovanni0.tables.tenant.AuthorTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid



class AuthorRepository
{
    fun findById(schema_name: String, id: Uuid): AuthorRow?
    {
        return tenantTransaction(schema_name)
        {
            AuthorTable
                .selectAll()
                .where { AuthorTable.id eq id }
                .map {
                    AuthorRow(
                        id = it[AuthorTable.id],
                        first_name = it[AuthorTable.first_name],
                        last_name = it[AuthorTable.last_name]
                    )
                }
                .firstOrNull()
        }
    }


    fun exists(schema_name: String, author_id: Uuid): Boolean
    {
        return tenantTransaction(schema_name)
        {
            AuthorTable
                .selectAll()
                .where { AuthorTable.id eq author_id }
                .empty()
                .not()
        }
    }


    fun findAll(tenant_schema: String): List<AuthorRow>
    {
        return tenantTransaction(tenant_schema)
        {
            AuthorTable
                .selectAll()
                .map {
                    AuthorRow(
                        id = it[AuthorTable.id],
                        first_name = it[AuthorTable.first_name],
                        last_name = it[AuthorTable.last_name]
                    )
                }
        }
    }


    fun create(tenant_schema: String, first_name: String?, last_name: String): AuthorRow
    {
        return tenantTransaction(tenant_schema)
        {
            val insert_statement = AuthorTable.insert {
                it[AuthorTable.first_name] = first_name
                it[AuthorTable.last_name] = last_name
            }

            AuthorRow(
                id = insert_statement[AuthorTable.id],
                first_name = insert_statement[AuthorTable.first_name],
                last_name = insert_statement[AuthorTable.last_name]
            )
        }
    }
}
