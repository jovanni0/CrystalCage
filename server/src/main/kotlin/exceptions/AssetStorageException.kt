package dev.jovanni0.exceptions



class AssetStorageException(override val message: String, cause: Throwable? = null): Exception(message, cause)

