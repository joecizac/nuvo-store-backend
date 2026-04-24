package com.nuvo.backend.common.exception

sealed class NuvoException(
    override val message: String,
    val code: Int
) : RuntimeException(message)

class ResourceNotFoundException(message: String) : NuvoException(message, 4040)

class ValidationException(message: String) : NuvoException(message, 4000)

class UnauthorizedException(message: String) : NuvoException(message, 4010)

class BusinessLogicException(message: String, code: Int = 4220) : NuvoException(message, code)
