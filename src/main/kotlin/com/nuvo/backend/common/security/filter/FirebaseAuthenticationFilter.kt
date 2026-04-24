package com.nuvo.backend.common.security.filter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseAuthenticationFilter(
    private val firebaseAuth: FirebaseAuth?,
    @Value("\${nuvo.mock-mode:false}") private val mockMode: Boolean
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (mockMode) {
            val authentication = UsernamePasswordAuthenticationToken(
                "mock-user-id",
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ") && firebaseAuth != null) {
            val idToken = authHeader.substring(7)
            try {
                val decodedToken = firebaseAuth.verifyIdToken(idToken)
                val uid = decodedToken.uid

                val authentication = UsernamePasswordAuthenticationToken(
                    uid, // Principal is the Firebase UID
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER"))
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication

            } catch (e: FirebaseAuthException) {
                logger.error("Firebase auth failed: \${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }
}
