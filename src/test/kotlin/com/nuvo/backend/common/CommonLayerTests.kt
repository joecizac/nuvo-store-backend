package com.nuvo.backend.common

import com.nuvo.backend.common.config.GlobalResponseWrapper
import com.nuvo.backend.common.dto.ApiResponse
import com.nuvo.backend.common.exception.BusinessLogicException
import com.nuvo.backend.common.exception.GlobalExceptionHandler
import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.exception.UnauthorizedException
import com.nuvo.backend.common.exception.ValidationException
import com.nuvo.backend.common.util.GeometryUtil
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

class CommonLayerTests {

    @Test
    fun `ApiResponse factories produce expected envelopes`() {
        val success = ApiResponse.success("payload")
        val error = ApiResponse.error<String>("Bad", "Details", 4000)

        assertEquals(2000, success.code)
        assertEquals("payload", success.data)
        assertEquals(4000, error.code)
        assertEquals("Details", error.error)
    }

    @Test
    fun `GeometryUtil creates longitude latitude point with srid`() {
        val point = GeometryUtil.createPoint(-33.9, 18.4)

        assertEquals(18.4, point.x)
        assertEquals(-33.9, point.y)
        assertEquals(4326, point.srid)
    }

    @Test
    fun `GlobalResponseWrapper wraps ordinary bodies and leaves ApiResponse unchanged`() {
        val wrapper = GlobalResponseWrapper()
        val body = mapOf("ok" to true)
        val wrapped = wrapper.beforeBodyWrite(
            body,
            Mockito.mock(MethodParameter::class.java),
            MediaType.APPLICATION_JSON,
            StringHttpMessageConverter::class.java,
            Mockito.mock(),
            Mockito.mock()
        ) as ApiResponse<*>

        assertEquals(body, wrapped.data)

        val response = ApiResponse.success("already")
        val unchanged = wrapper.beforeBodyWrite(
            response,
            Mockito.mock(MethodParameter::class.java),
            MediaType.APPLICATION_JSON,
            StringHttpMessageConverter::class.java,
            Mockito.mock(),
            Mockito.mock()
        )
        assertSame(response, unchanged)
    }

    @Test
    fun `GlobalResponseWrapper does not support ApiResponse return type`() {
        val wrapper = GlobalResponseWrapper()
        val returnType = Mockito.mock(MethodParameter::class.java)
        Mockito.`when`(returnType.parameterType).thenReturn(ApiResponse::class.java)

        assertFalse(wrapper.supports(returnType, StringHttpMessageConverter::class.java))
    }

    @Test
    fun `GlobalExceptionHandler maps domain exceptions to statuses`() {
        val handler = GlobalExceptionHandler()

        assertEquals(HttpStatus.NOT_FOUND, handler.handleNuvoException(ResourceNotFoundException("Missing"), Mockito.mock()).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, handler.handleNuvoException(UnauthorizedException("No"), Mockito.mock()).statusCode)
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleNuvoException(ValidationException("Invalid"), Mockito.mock()).statusCode)
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleNuvoException(BusinessLogicException("Rule"), Mockito.mock()).statusCode)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.handleAllExceptions(RuntimeException("Boom"), Mockito.mock()).statusCode)
    }
}
