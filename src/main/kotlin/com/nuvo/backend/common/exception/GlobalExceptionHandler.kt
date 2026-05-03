package com.nuvo.backend.common.exception

import com.nuvo.backend.common.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NuvoException::class)
    fun handleNuvoException(ex: NuvoException, request: WebRequest): ResponseEntity<ApiResponse<Any>> {
        val status = when (ex) {
            is ResourceNotFoundException -> HttpStatus.NOT_FOUND
            is UnauthorizedException -> HttpStatus.UNAUTHORIZED
            is ValidationException, is BusinessLogicException -> HttpStatus.BAD_REQUEST
        }
        val apiResponse = ApiResponse.error<Any>(
            message = ex.message,
            errorCode = ex.code.toString()
        )
        return ResponseEntity(apiResponse, status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ApiResponse<Any>> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        val apiResponse = ApiResponse.error<Any>(
            message = "Validation Failed",
            errorCode = "4000"
        )
        return ResponseEntity(apiResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Any>> {
        val apiResponse = ApiResponse.error<Any>(
            message = "Invalid value for parameter '${ex.name}'",
            errorCode = "4001"
        )
        return ResponseEntity(apiResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(): ResponseEntity<ApiResponse<Any>> {
        val apiResponse = ApiResponse.error<Any>(
            message = "Malformed request body",
            errorCode = "4002"
        )
        return ResponseEntity(apiResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<ApiResponse<Any>> {
        val apiResponse = ApiResponse.error<Any>(
            message = "Internal Server Error",
            errorCode = "5000"
        )
        return ResponseEntity(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
