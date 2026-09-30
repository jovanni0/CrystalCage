package dev.jovanni0.database.transactions

import dev.jovanni0.data_types.BookOwnershipContext
import dev.jovanni0.exceptions.AuthorMismatchException
import dev.jovanni0.exceptions.not_found.AuthorNotFoundException
import dev.jovanni0.exceptions.not_found.SeriesNotFoundException
import dev.jovanni0.exceptions.not_found.UniverseNotFoundException
import dev.jovanni0.tables.tenant.AuthorTable
import dev.jovanni0.tables.tenant.SeriesTable
import dev.jovanni0.tables.tenant.UniverseTable
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid



/**
 * validates that `author_id`, `universe_id` and `series_id` are internally consistent (if a universe or a series is set,
 * they belong to the same author). throws `AuthorNotFoundException`, `UniverseNotFoundException`, `SeriesNotFoundException`
 * on missing reference and `AuthorMismatchException` if internally inconsistent.
 *
 * returns the resolved names for author, universe and series.
 */
fun Transaction.validateAndResolveBookOwnership(
    author_id: Uuid,
    universe_id: Uuid?,
    series_id: Uuid?,
): BookOwnershipContext
{
    val author = AuthorTable
        .selectAll()
        .where { AuthorTable.id eq author_id }
        .map { it[AuthorTable.first_name] to it[AuthorTable.last_name] }
        .firstOrNull()
        ?: throw AuthorNotFoundException(author_id.toString())

    val universe = if (universe_id != null)
        UniverseTable
            .select(UniverseTable.name, UniverseTable.author_id)
            .where { UniverseTable.id eq universe_id }
            .map { it[UniverseTable.name] to it[UniverseTable.author_id] }
            .firstOrNull()
            ?: throw UniverseNotFoundException(universe_id.toString())
    else null

    val series = if (series_id != null)
        SeriesTable
            .select(SeriesTable.name, SeriesTable.author_id)
            .where { SeriesTable.id eq series_id }
            .map { it[SeriesTable.name] to it[SeriesTable.author_id] }
            .firstOrNull()
            ?: throw SeriesNotFoundException(series_id.toString())
    else null

    if (universe != null && universe.second != author_id)
    {
        throw AuthorMismatchException("universe author_id ${universe.second} does not match book author_id ${author_id}")
    }

    if (series != null && series.second != author_id)
    {
        throw AuthorMismatchException("series author_id ${series.second} does not match book author_id ${author_id}")
    }

    return BookOwnershipContext(
        author_first_name = author.first,
        author_last_name = author.second,
        universe_name = universe?.first,
        series_name = series?.first,
    )
}
