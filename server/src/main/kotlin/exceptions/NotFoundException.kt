package dev.jovanni0.exceptions



sealed class NotFoundException(override val message: String): Exception(message)
