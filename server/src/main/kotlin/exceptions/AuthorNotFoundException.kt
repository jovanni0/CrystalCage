package dev.jovanni0.exceptions



class AuthorNotFoundException(id: String): NotFoundException("author $id does not exist")
