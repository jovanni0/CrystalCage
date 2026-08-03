package dev.jovanni0.repositories

import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.filesystem.BookFilenameContext
import dev.jovanni0.records.book.BookDetailRow
import dev.jovanni0.records.book.BookSummaryRow
import dev.jovanni0.tables.tenant.AuthorTable
import dev.jovanni0.tables.tenant.BookTable
import dev.jovanni0.tables.tenant.SeriesTable
import dev.jovanni0.tables.tenant.UniverseTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.awt.print.Book
import kotlin.uuid.Uuid



class BookRepository
{
    /**
     * TODO: temporary implementation for use in unit tests. needs to be expanded.
     */
    fun findById(schema_name: String, id: Uuid): BookDetailRow?
    {
        return tenantTransaction(schema_name)
        {
            BookTable
                .selectAll()
                .where { BookTable.id eq id }
                .map {
                    BookDetailRow(
                        id = it[BookTable.id],
                        author_id = it[BookTable.author_id],
                        series_id = it[BookTable.series_id],
                        universe_id = it[BookTable.universe_id],
                        cover_id = it[BookTable.cover_id],
                        volume_number = it[BookTable.volume_number]?.toFloat(),
                        title = it[BookTable.title],
                        launch_date = it[BookTable.launch_date],
                        word_count = it[BookTable.word_count],
                        blurb = it[BookTable.blurb],
                        notes = it[BookTable.notes],
                        tag_ids = listOf(),
                        read_status = ReadStatus.UNREAD,
                        reread_count = 0,
                    )
                }
                .firstOrNull()
        }
    }


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


    fun getBookContext(schema_name: String, id: Uuid): BookFilenameContext?
    {
        return tenantTransaction(schema_name)
        {
            BookTable
                .join(
                    AuthorTable,
                    JoinType.INNER,
                    BookTable.author_id,
                    AuthorTable.id
                )
                .join(
                    UniverseTable,
                    JoinType.LEFT,
                    BookTable.universe_id,
                    UniverseTable.id
                )
                .join(
                    SeriesTable,
                    JoinType.LEFT,
                    BookTable.series_id,
                    SeriesTable.id
                )
                .select(AuthorTable.first_name, AuthorTable.last_name, UniverseTable.name, SeriesTable.name, BookTable.volume_number, BookTable.title)
                .where { BookTable.id eq id }
                .map {
                    BookFilenameContext(
                        first_name = it[AuthorTable.first_name],
                        last_name = it[AuthorTable.last_name],
                        universe = it[UniverseTable.name],
                        series = it[SeriesTable.name],
                        volume_number = it[BookTable.volume_number]?.toFloat(),
                        title = it[BookTable.title],
                    )
                }
                .firstOrNull()
        }
    }
}
