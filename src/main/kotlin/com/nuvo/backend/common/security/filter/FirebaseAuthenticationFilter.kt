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
        val authHeader = request.getHeader("Authorization")

        if (mockMode) {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                val authorities = mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
                if (token == "admin" || token == "mock-admin" || request.getHeader("X-Mock-Admin") == "true") {
                    authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
                }
                val authentication = UsernamePasswordAuthenticationToken(
                    "mock-user-id",
                    null,
                    authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
            filterChain.doFilter(request, response)
            return
        }

        if (authHeader != null && authHeader.startsWith("Bearer ") && firebaseAuth != null) {
            val idToken = authHeader.substring(7)
            try {
                val decodedToken = firebaseAuth.verifyIdToken(idToken)
                val uid = decodedToken.uid
                val authorities = mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
                val adminClaim = decodedToken.claims["admin"] as? Boolean == true
                val roleClaim = decodedToken.claims["role"]?.toString()
                val rolesClaim = decodedToken.claims["roles"] as? Collection<*>
                if (
                    adminClaim ||
                    roleClaim.equals("ADMIN", ignoreCase = true) ||
                    rolesClaim?.any { it.toString().equals("ADMIN", ignoreCase = true) } == true
                ) {
                    authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
                }

                val authentication = UsernamePasswordAuthenticationToken(
                    uid,
                    null,
                    authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication

            } catch (e: FirebaseAuthException) {
                logger.error("Firebase auth failed: ${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }
}
