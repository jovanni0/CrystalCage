package dev.jovanni0.exceptions.not_found



class ReadRecordNotFoundException(id: String): NotFoundException("read record $id does not exist")