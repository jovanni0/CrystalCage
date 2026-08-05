package dev.jovanni0.exceptions.not_found



class BookNotFoundException(id: String): NotFoundException("cover $id does not exist")
