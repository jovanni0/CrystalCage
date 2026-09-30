package dev.jovanni0.data_types

import kotlinx.serialization.Serializable



@Serializable
data class DiscoveryReply(
    val name: String,
    val port: Int,
    val https: Boolean = false
)