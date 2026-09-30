package dev.jovanni0.exceptions.not_found



sealed class NotFoundException(override val message: String): Exception(message)
