package com.blinddate.common.exception

open class BusinessException(val status: Int, override val message: String) : RuntimeException(message)
class NotFoundException(message: String) : BusinessException(404, message)
class BadRequestException(message: String) : BusinessException(400, message)
class ConflictException(message: String) : BusinessException(409, message)
class ForbiddenException(message: String) : BusinessException(403, message)
class UnauthorizedException(message: String) : BusinessException(401, message)
