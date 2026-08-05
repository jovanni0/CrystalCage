package dev.jovanni0.data_types

import java.time.LocalDate
import kotlin.uuid.Uuid



data class BookCreateContext(
    val author_id: Uuid,
    val universe_id: Uuid? = null,
    val series_id: Uuid? = null,
    val volume_number: Float? = null,
    val title: String,
    val launch_date: LocalDate? = null,
    val word_count: Int? = null,
    val blurb: String? = null,
    val notes: String? = null,
    val tag_ids: List<Uuid> = emptyList(),
    val covers: List<CoverCreateContext> = emptyList(),
    val editions: List<EditionCreateContext> = emptyList(),
)
