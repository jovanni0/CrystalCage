package dev.jovanni0.repositories


import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.edition.EditionRow
import dev.jovanni0.tables.tenant.EditionTable
import org.jetbrains.exposed.v1.jdbc.insert
import java.time.LocalDate
import kotlin.uuid.Uuid



class EditionRepository
{
    fun create(
        schema_name: String,
        book_id: Uuid,
        label: String?,
        release_date: LocalDate?,
        format: String,
    ): EditionRow
    {
        return tenantTransaction(schema_name)
        {
            val insert_statement = EditionTable.insert {
                it[EditionTable.book_id] = book_id
                it[EditionTable.label] = label
                it[EditionTable.release_date] = release_date
                it[EditionTable.added_at] = LocalDate.now()
                it[EditionTable.format] = format
            }

            EditionRow(
                id = insert_statement[EditionTable.id],
                book_id = insert_statement[EditionTable.book_id],
                label = insert_statement[EditionTable.label],
                release_date = insert_statement[EditionTable.release_date],
                format = insert_statement[EditionTable.format],
                added_at = insert_statement[EditionTable.added_at],
            )
        }
    }
}
