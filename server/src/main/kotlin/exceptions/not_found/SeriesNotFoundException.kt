package dev.jovanni0.exceptions.not_found



class SeriesNotFoundException(id: String): NotFoundException("series $id does not exist")
