package dev.jovanni0.exceptions.not_found



class UniverseNotFoundException(id: String): NotFoundException("universe $id does not exist")
