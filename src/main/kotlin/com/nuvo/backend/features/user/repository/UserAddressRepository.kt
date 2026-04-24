package com.nuvo.backend.features.user.repository

import com.nuvo.backend.features.user.domain.UserAddress
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserAddressRepository : JpaRepository<UserAddress, UUID> {
    fun findAllByUserId(userId: UUID): List<UserAddress>
    fun findByUserIdAndIsDefaultTrue(userId: UUID): UserAddress?
}
