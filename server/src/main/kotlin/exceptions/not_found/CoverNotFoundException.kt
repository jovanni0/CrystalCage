package dev.jovanni0.exceptions.not_found



class CoverNotFoundException(id: String): NotFoundException("cover $id does not exist")
