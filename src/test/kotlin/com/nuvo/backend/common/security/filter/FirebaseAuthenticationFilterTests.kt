package com.nuvo.backend.common.security.filter

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FirebaseAuthenticationFilterTests {

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `mock mode leaves request anonymous without bearer token`() {
        val filter = FirebaseAuthenticationFilter(null, true)
        val chain = Mockito.mock(FilterChain::class.java)

        filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(chain).doFilter(Mockito.any(), Mockito.any())
    }

    @Test
    fun `mock mode authenticates bearer user`() {
        val filter = FirebaseAuthenticationFilter(null, true)
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer user")

        filter.doFilter(request, MockHttpServletResponse(), Mockito.mock(FilterChain::class.java))

        val authentication = SecurityContextHolder.getContext().authentication!!
        assertEquals("mock-user-id", authentication.name)
        assertTrue(authentication.authorities.any { it.authority == "ROLE_USER" })
        assertFalse(authentication.authorities.any { it.authority == "ROLE_ADMIN" })
    }

    @Test
    fun `mock mode grants admin role for mock-admin bearer token`() {
        val filter = FirebaseAuthenticationFilter(null, true)
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer mock-admin")

        filter.doFilter(request, MockHttpServletResponse(), Mockito.mock(FilterChain::class.java))

        val authentication = SecurityContextHolder.getContext().authentication!!
        assertTrue(authentication.authorities.any { it.authority == "ROLE_ADMIN" })
    }
}
