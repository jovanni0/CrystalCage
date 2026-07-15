package dev.jovanni0.data_types.universe.response

import kotlinx.serialization.Serializable



@Serializable
data class UniverseSummaryResponse(
    val id: String,
    val name: String,
)
