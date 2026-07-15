package dev.jovanni0.exceptions



class UniverseNotFoundException(id: String): NotFoundException("universe $id does not exist")
