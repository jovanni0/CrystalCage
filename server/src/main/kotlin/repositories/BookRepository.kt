package dev.jovanni0.repositories

import dev.jovanni0.data_types.BookCreateContext
import dev.jovanni0.data_types.ReadRecordAggregate
import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.database.transactions.validateAndResolveBookOwnership
import dev.jovanni0.exceptions.not_found.BookNotFoundException
import dev.jovanni0.exceptions.not_found.CoverNotFoundException
import dev.jovanni0.filesystem.BookFilenameContext
import dev.jovanni0.records.book.BookDetailRow
import dev.jovanni0.records.book.BookSummaryRow
import dev.jovanni0.tables.tenant.AuthorTable
import dev.jovanni0.tables.tenant.BookTable
import dev.jovanni0.tables.tenant.BookTagTable
import dev.jovanni0.tables.tenant.CoverTable
import dev.jovanni0.tables.tenant.ReadRecordTable
import dev.jovanni0.tables.tenant.SeriesTable
import dev.jovanni0.tables.tenant.UniverseTable
import dev.jovanni0.utils.Update
import dev.jovanni0.utils.computeReadStatus
import dev.jovanni0.utils.updateIfPresent
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.compoundAnd
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.collections.map
import kotlin.uuid.Uuid



class BookRepository(
    private val author_repository: AuthorRepository,
    private val cover_repository: CoverRepository,
    private val edition_repository: EditionRepository,
)
{
    fun findAllBy(
        schema_name: String,
        author_id: Uuid?,
        universe_id: Uuid?,
        series_id: Uuid?,
        tag_id: Uuid?,
    ): List<BookSummaryRow>
    {
        return tenantTransaction(schema_name)
        {
            val filter = listOfNotNull(
                author_id?.let { BookTable.author_id eq it },
                universe_id?.let { BookTable.universe_id eq it },
                series_id?.let { BookTable.series_id eq it },
                tag_id?.let { BookTagTable.tag_id eq it },
            )

            var query = BookTable
                .join(AuthorTable, JoinType.INNER, BookTable.author_id, AuthorTable.id)
                .join(UniverseTable, JoinType.LEFT, BookTable.universe_id, UniverseTable.id)
                .join(SeriesTable, JoinType.LEFT, BookTable.series_id, SeriesTable.id)

            if (tag_id != null)
            {
                query = query.join(BookTagTable, JoinType.INNER, BookTable.id, BookTagTable.book_id)
            }

            var selection = query
                .selectAll()
                .orderBy(
                    AuthorTable.last_name to SortOrder.ASC,
                    AuthorTable.first_name to SortOrder.ASC_NULLS_LAST,
                    UniverseTable.name to SortOrder.ASC_NULLS_LAST,
                    SeriesTable.name to SortOrder.ASC_NULLS_LAST,
                    BookTable.volume_number to SortOrder.ASC_NULLS_LAST,
                    BookTable.title to SortOrder.ASC,
                )

            if (filter.isNotEmpty())
            {
                selection = selection.andWhere { filter.compoundAnd() }
            }

            val book_ids = mutableListOf<Uuid>()
            val incomplete_response = selection.map {
                book_ids += it[BookTable.id]

                BookSummaryRow(
                    id = it[BookTable.id],
                    author_id = it[BookTable.author_id],
                    series_id = it[BookTable.series_id],
                    universe_id = it[BookTable.universe_id],
                    cover_id = it[BookTable.cover_id],
                    volume_number = it[BookTable.volume_number]?.toFloat(),
                    title = it[BookTable.title],
                    read_status = ReadStatus.UNREAD,
                    reread_count = 0,
                    word_count = it[BookTable.word_count],
                )
            }

            val read_records = ReadRecordTable
                .selectAll()
                .where { ReadRecordTable.book_id inList book_ids }
                .map {
                    it[ReadRecordTable.book_id] to ReadRecordAggregate(
                        end_date = it[ReadRecordTable.end_date],
                        is_historical = it[ReadRecordTable.is_historical],
                        dnf = it[ReadRecordTable.did_not_finish],
                    )
                }
                .groupBy({ it.first }, { it.second })

            incomplete_response.map {
                val (read_status, read_count) = computeReadStatus(read_records[it.id] ?: emptyList())

                it.copy(
                    read_status = read_status,
                    reread_count = read_count
                )
            }
        }
    }


    fun findById(schema_name: String, id: Uuid): BookDetailRow?
    {
        return tenantTransaction(schema_name)
        {
            val read_records = ReadRecordTable
                .selectAll()
                .where { ReadRecordTable.book_id eq id }
                .map {
                    ReadRecordAggregate(
                        end_date = it[ReadRecordTable.end_date],
                        is_historical = it[ReadRecordTable.is_historical],
                        dnf = it[ReadRecordTable.did_not_finish],
                    )
                }

            val tags = BookTagTable
                .selectAll()
                .where { BookTagTable.book_id eq id }
                .map { it[BookTagTable.tag_id] }

            BookTable
                .selectAll()
                .where { BookTable.id eq id }
                .map {
                    val (read_status, read_count) = computeReadStatus(read_records)

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
                        tag_ids = tags,
                        read_status = read_status,
                        reread_count = read_count
                    )
                }
                .firstOrNull()
        }
    }


    /**
     * TODO: if the transaction fails after files have been saved to disk, they remain orphan until the reconciliation job runs.
     */
    fun create(schema_name: String, context: BookCreateContext): BookSummaryRow
    {
        return tenantTransaction(schema_name)
        {
            val book_ownership_context = validateAndResolveBookOwnership(context.author_id, context.universe_id, context.series_id)

            val insert_statement = BookTable.insert {
                it[BookTable.author_id] = context.author_id
                it[BookTable.universe_id] = context.universe_id
                it[BookTable.series_id] = context.series_id
                it[BookTable.volume_number] = context.volume_number?.toBigDecimal()
                it[BookTable.title] = context.title
                it[BookTable.launch_date] = context.launch_date
                it[BookTable.word_count] = context.word_count
                it[BookTable.blurb] = context.blurb
                it[BookTable.notes] = context.notes
            }

            if (context.tag_ids.isNotEmpty())
            {
                BookTagTable.batchInsert(context.tag_ids.toSet()) { tag_id ->
                    this[BookTagTable.book_id] = insert_statement[BookTable.id]
                    this[BookTagTable.tag_id] = tag_id
                }
            }

            val book_context = BookFilenameContext(
                book_ownership_context.author_first_name,
                book_ownership_context.author_last_name,
                book_ownership_context.universe_name,
                book_ownership_context.series_name,
                context.volume_number,
                context.title
            )

            var default_cover_id: Uuid? = null
            context.covers.forEach {
                val cover = cover_repository.create(
                    schema_name,
                    insert_statement[BookTable.id],
                    it.label,
                    it.filename,
                    it.file,
                    book_context,
                )

                if (it.is_default)
                {
                    default_cover_id = cover.id
                }
            }

            if (default_cover_id != null)
            {
                BookTable.update({ BookTable.id eq insert_statement[BookTable.id] })
                {
                    it[BookTable.cover_id] = default_cover_id
                }
            }

            context.editions.forEach {
                edition_repository.create(
                    schema_name,
                    insert_statement[BookTable.id],
                    it.label,
                    it.release_date,
                    it.format,
                    it.filename,
                    it.file,
                    book_context,
                )
            }

            BookSummaryRow(
                id = insert_statement[BookTable.id],
                author_id = insert_statement[BookTable.author_id],
                series_id = insert_statement[BookTable.series_id],
                universe_id = insert_statement[BookTable.universe_id],
                cover_id = default_cover_id,
                volume_number = insert_statement[BookTable.volume_number]?.toFloat(),
                title = insert_statement[BookTable.title],
                read_status = ReadStatus.UNREAD,
                reread_count = 0,
                word_count = insert_statement[BookTable.word_count],
            )
        }
    }


    fun update(
        schema_name: String,
        id: Uuid,
        author_id: Update<Uuid>,
        universe_id: Update<Uuid?>,
        series_id: Update<Uuid?>,
        cover_id: Update<Uuid?>,
        volume_number: Update<BigDecimal?>,
        title: Update<String>,
        launch_date: Update<LocalDate?>,
        word_count: Update<Int?>,
        blurb: Update<String?>,
        notes: Update<String?>,
        tag_ids: Update<List<Uuid>>,
    ): BookDetailRow?
    {
        return tenantTransaction(schema_name)
        {
            val current_record = BookTable
                .selectAll()
                .where { BookTable.id eq id }
                .firstOrNull()
                ?: throw BookNotFoundException(id.toString())

            val effective_author_id = when (author_id)
            {
                is Update.Value -> author_id.value
                is Update.Undefined -> current_record[BookTable.author_id]
            }

            val effective_universe_id = when (universe_id)
            {
                is Update.Value -> universe_id.value
                is Update.Undefined -> current_record[BookTable.universe_id]
            }

            val effective_series_id = when (series_id)
            {
                is Update.Value -> series_id.value
                is Update.Undefined -> current_record[BookTable.series_id]
            }

            val does_changge_fields = author_id !is Update.Undefined ||
                universe_id !is Update.Undefined ||
                series_id !is Update.Undefined

            if (does_changge_fields)
            {
                validateAndResolveBookOwnership(effective_author_id, effective_universe_id, effective_series_id)
            }

            if (cover_id is Update.Value && cover_id.value != null)
            {
                val does_cover_belong_to_book = CoverTable
                    .select(CoverTable.id)
                    .where { (CoverTable.id eq cover_id.value) and (CoverTable.book_id eq id) }
                    .any()

                if (!does_cover_belong_to_book)
                {
                    throw CoverNotFoundException(cover_id.value.toString())
                }
            }

            if (tag_ids is Update.Value)
            {
                val current_tags = BookTagTable
                    .selectAll()
                    .where { BookTagTable.book_id eq id }
                    .map { it[BookTagTable.tag_id] }

                val missing_tags = current_tags.filter { !tag_ids.value.contains(it) }.toSet()
                val new_tags = tag_ids.value.filter { !current_tags.contains(it) }.toSet()

                if (missing_tags.isNotEmpty())
                {
                    BookTagTable.deleteWhere { (BookTagTable.tag_id inList missing_tags) and (BookTagTable.book_id eq id) }
                }

                if (new_tags.isNotEmpty())
                {
                    BookTagTable.batchInsert(new_tags){ tag_id ->
                        this[BookTagTable.book_id] = id
                        this[BookTagTable.tag_id] = tag_id
                    }
                }
            }

            val is_data_uo_update = listOf(author_id, universe_id, series_id, cover_id, volume_number, title, launch_date, word_count, blurb, notes)
                .any { it !is Update.Undefined }

            if (is_data_uo_update)
            {
                BookTable.update({ BookTable.id eq id })
                {
                    it.updateIfPresent(BookTable.author_id, author_id)
                    it.updateIfPresent(BookTable.universe_id, universe_id)
                    it.updateIfPresent(BookTable.series_id, series_id)
                    it.updateIfPresent(BookTable.cover_id, cover_id)
                    it.updateIfPresent(BookTable.volume_number, volume_number)
                    it.updateIfPresent(BookTable.title, title)
                    it.updateIfPresent(BookTable.launch_date, launch_date)
                    it.updateIfPresent(BookTable.word_count, word_count)
                    it.updateIfPresent(BookTable.blurb, blurb)
                    it.updateIfPresent(BookTable.notes, notes)
                }
            }

            findById(schema_name, id)
        }
    }


    fun delete(schema_name: String, id: Uuid): Boolean
    {
        return tenantTransaction(schema_name)
        {
            BookTable.deleteWhere { BookTable.id eq id } > 0
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


    fun exists(schema_name: String, id: Uuid): Boolean
    {
        return tenantTransaction(schema_name)
        {
            BookTable
                .selectAll()
                .where { BookTable.id eq id }
                .empty()
                .not()
        }
    }
}
