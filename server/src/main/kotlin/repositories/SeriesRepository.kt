package dev.jovanni0.repositories

import dev.jovanni0.data_types.ReadRecordAggregate
import dev.jovanni0.data_types.book.BookSummaryResponse
import dev.jovanni0.data_types.series.SeriesDetailResponse
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.series.SeriesRow
import dev.jovanni0.tables.tenant.BookTable
import dev.jovanni0.tables.tenant.ReadRecordTable
import dev.jovanni0.tables.tenant.SeriesTable
import dev.jovanni0.utils.computeReadStatus
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.compoundAnd
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid



class SeriesRepository
{
    fun findAllByAuthorIdAndUniverseId(schema_name: String, author_id: Uuid?, universe_id: Uuid?): List<SeriesRow>
    {
        return tenantTransaction(schema_name)
        {
            val condition = listOfNotNull(
                author_id?.let { SeriesTable.author_id eq author_id },
                universe_id?.let { SeriesTable.universe_id eq universe_id },
            )

            val query = SeriesTable.selectAll()

            if (condition.isNotEmpty())
            {
                query.where { condition.compoundAnd() }
            }

            query
                .orderBy(SeriesTable.name)
                .map {
                    SeriesRow(
                        id = it[SeriesTable.id],
                        author_id = it[SeriesTable.author_id],
                        universe_id = it[SeriesTable.universe_id],
                        name = it[SeriesTable.name],
                        is_finished = it[SeriesTable.is_finished],
                    )
                }
        }
    }


    fun findById(schema_name: String, id: Uuid): SeriesDetailResponse?
    {
        return tenantTransaction(schema_name)
        {
            val rows = SeriesTable
                .join(
                    BookTable,
                    JoinType.LEFT,
                    onColumn = SeriesTable.id,
                    otherColumn = BookTable.series_id
                )
                .join(
                    ReadRecordTable,
                    JoinType.LEFT,
                    onColumn = BookTable.id,
                    otherColumn = ReadRecordTable.book_id
                )
                .select(
                    SeriesTable.id, SeriesTable.author_id, SeriesTable.universe_id, SeriesTable.name, SeriesTable.is_finished,
                    BookTable.id, BookTable.author_id, BookTable.series_id, BookTable.universe_id, BookTable.cover_id, BookTable.volume_number, BookTable.title,
                    ReadRecordTable.id, ReadRecordTable.end_date, ReadRecordTable.did_not_finish, ReadRecordTable.is_historical
                )
                .where { SeriesTable.id eq id }
                .toList()

            if (rows.isEmpty())
                return@tenantTransaction null

            val series_row = rows.first()

            val books = rows
                .filter { it[BookTable.id] != null }
                .groupBy { it[BookTable.id] }
                .map { (book_id, book_rows) ->
                    val first = book_rows.first()
                    val records = book_rows
                        .filter { it[ReadRecordTable.id] != null }
                        .map {
                            ReadRecordAggregate(
                                end_date = it[ReadRecordTable.end_date],
                                dnf = it[ReadRecordTable.did_not_finish],
                                is_historical = it[ReadRecordTable.is_historical]
                            )
                        }

                    val (status, reread_count) = computeReadStatus(records)

                    BookSummaryResponse(
                        id = book_id.toString(),
                        author_id = first[BookTable.author_id].toString(),
                        series_id = first[BookTable.series_id]?.toString(),
                        universe_id = first[BookTable.universe_id]?.toString(),
                        cover_id = first[BookTable.cover_id]?.toString(),
                        volume_number = first[BookTable.volume_number]?.toFloat(),
                        title = first[BookTable.title],
                        read_status = status,
                        reread_count = reread_count
                    )
                }

            SeriesDetailResponse(
                id = series_row[SeriesTable.id].toString(),
                author_id = series_row[SeriesTable.author_id].toString(),
                universe_id = series_row[SeriesTable.universe_id]?.toString(),
                name = series_row[SeriesTable.name],
                is_finished = series_row[SeriesTable.is_finished],
                books = books
            )
        }
    }


    fun create(schema_name: String, author_id: Uuid, universe_id: Uuid?, name: String, is_finished: Boolean): SeriesRow
    {
        return tenantTransaction(schema_name)
        {
            val insert_statement = SeriesTable.insert {
                it[SeriesTable.author_id] = author_id
                it[SeriesTable.universe_id] = universe_id
                it[SeriesTable.name] = name
                it[SeriesTable.is_finished] = is_finished
            }

            SeriesRow(
                id = insert_statement[SeriesTable.id],
                author_id = insert_statement[SeriesTable.author_id],
                universe_id = insert_statement[SeriesTable.universe_id],
                name = insert_statement[SeriesTable.name],
                is_finished =  insert_statement[SeriesTable.is_finished],
            )
        }
    }
}
