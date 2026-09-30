package dev.jovanni0.data_types

import kotlinx.serialization.Serializable



@Serializable
enum class ReadStatus
{
    UNREAD,
    IN_PROGRESS,
    DNF,
    READ,
}
