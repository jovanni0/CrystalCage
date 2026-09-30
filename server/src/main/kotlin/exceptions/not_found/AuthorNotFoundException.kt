package dev.jovanni0.exceptions.not_found



class AuthorNotFoundException(id: String): NotFoundException("author $id does not exist")
