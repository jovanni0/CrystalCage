package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.author.AuthorRow
import dev.jovanni0.tables.tenant.AuthorTable
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll



class AuthorRepository
{
    fun findAll(tenant_schema: String): List<AuthorRow>
    {
        return tenantTransaction(tenant_schema)
        {
            AuthorTable
                .selectAll()
                .map {
                    AuthorRow(
                        id = it[AuthorTable.id].toString(),
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
                id = insert_statement[AuthorTable.id].toString(),
                first_name = insert_statement[AuthorTable.first_name],
                last_name = insert_statement[AuthorTable.last_name]
            )
        }
    }
}
