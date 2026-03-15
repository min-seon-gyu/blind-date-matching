package com.blinddate.common.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    val status: HttpStatus,
    override val message: String
) : RuntimeException(message)

class NotFoundException(message: String) : BusinessException(HttpStatus.NOT_FOUND, message)
class BadRequestException(message: String) : BusinessException(HttpStatus.BAD_REQUEST, message)
class ForbiddenException(message: String) : BusinessException(HttpStatus.FORBIDDEN, message)
class UnauthorizedException(message: String) : BusinessException(HttpStatus.UNAUTHORIZED, message)
class ConflictException(message: String) : BusinessException(HttpStatus.CONFLICT, message)
