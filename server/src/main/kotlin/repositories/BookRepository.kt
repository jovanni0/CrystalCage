package dev.jovanni0.repositories

import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.book.BookSummaryRow
import dev.jovanni0.tables.tenant.BookTable
import org.jetbrains.exposed.v1.jdbc.insert
import kotlin.uuid.Uuid



class BookRepository
{
    /**
     * TODO: temporary implementation for use in unit tests. needs to be expanded.
     */
    fun create(schema_name: String, author_id: Uuid, universe_id: Uuid?, series_id: Uuid?, title: String): BookSummaryRow
    {
        return tenantTransaction(schema_name)
        {
            val insert_statement = BookTable.insert {
                it[BookTable.author_id] = author_id
                it[BookTable.universe_id] = universe_id
                it[BookTable.series_id] = series_id
                it[BookTable.title] = title
            }

            BookSummaryRow(
                id = insert_statement[BookTable.id],
                author_id = insert_statement[BookTable.author_id],
                series_id = insert_statement[BookTable.series_id],
                universe_id = insert_statement[BookTable.universe_id],
                cover_id = insert_statement[BookTable.cover_id],
                volume_number = insert_statement[BookTable.volume_number]?.toFloat(),
                title = insert_statement[BookTable.title],
                read_status = ReadStatus.UNREAD,
                reread_count = 0,
            )
        }
    }
}
