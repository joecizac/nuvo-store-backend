package com.nuvo.backend.common.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
    val error: String? = null
) {
    companion object {
        fun <T> success(data: T?, message: String = "Success", code: Int = 2000): ApiResponse<T> =
            ApiResponse(code = code, message = message, data = data)

        fun <T> error(message: String, error: String? = null, code: Int = 5000): ApiResponse<T> =
            ApiResponse(code = code, message = message, error = error)
    }
}
