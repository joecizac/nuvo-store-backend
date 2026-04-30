package com.nuvo.backend.features.user.service

import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.util.GeometryUtil
import com.nuvo.backend.features.user.domain.User
import com.nuvo.backend.features.user.domain.UserAddress
import com.nuvo.backend.features.user.dto.AddressDTO
import com.nuvo.backend.features.user.dto.SyncUserRequest
import com.nuvo.backend.features.user.dto.UserProfileDTO
import com.nuvo.backend.features.user.repository.UserAddressRepository
import com.nuvo.backend.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val addressRepository: UserAddressRepository
) {

    @Transactional
    fun syncUser(firebaseUid: String, request: SyncUserRequest): UserProfileDTO {
        val user = userRepository.findByFirebaseUid(firebaseUid)
            ?.apply {
                this.name = request.name
                this.email = request.email
                request.profileImageUrl?.let { this.profileImageUrl = it }
            }
            ?: userRepository.save(
                User(
                    firebaseUid = firebaseUid,
                    name = request.name,
                    email = request.email,
                    profileImageUrl = request.profileImageUrl
                )
            )

        return user.toDTO()
    }

    @Transactional(readOnly = true)
    fun getUserProfile(firebaseUid: String): UserProfileDTO {
        return userRepository.findByFirebaseUid(firebaseUid)?.toDTO()
            ?: throw ResourceNotFoundException("User not found")
    }

    @Transactional
    fun updateFcmToken(firebaseUid: String, token: String) {
        userRepository.findByFirebaseUid(firebaseUid)?.apply {
            this.fcmToken = token
            userRepository.save(this)
        }
    }

    @Transactional
    fun addAddress(firebaseUid: String, dto: AddressDTO): AddressDTO {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val userId = user.id ?: throw ResourceNotFoundException("User ID missing")

        if (dto.isDefault) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)?.apply {
                this.isDefault = false
                addressRepository.save(this)
            }
        }

        val address = UserAddress(
            user = user,
            title = dto.title,
            fullAddress = dto.fullAddress,
            location = GeometryUtil.createPoint(dto.latitude, dto.longitude),
            isDefault = dto.isDefault
        )
        return addressRepository.save(address).toDTO()
    }

    @Transactional(readOnly = true)
    fun getUserAddresses(firebaseUid: String): List<AddressDTO> {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val userId = user.id ?: throw ResourceNotFoundException("User ID missing")
        return addressRepository.findAllByUserId(userId).map { it.toDTO() }
    }

    @Transactional
    fun updateAddress(firebaseUid: String, addressId: UUID, dto: AddressDTO): AddressDTO {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val address = addressRepository.findById(addressId).orElseThrow { ResourceNotFoundException("Address not found") }
        
        if (address.user.id != user.id) throw ResourceNotFoundException("Address not found for this user")

        address.title = dto.title
        address.fullAddress = dto.fullAddress
        address.location = GeometryUtil.createPoint(dto.latitude, dto.longitude)
        
        // If updating to default, unset previous default
        if (dto.isDefault && !address.isDefault) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.id!!)?.apply {
                this.isDefault = false
                addressRepository.save(this)
            }
        }
        address.isDefault = dto.isDefault

        return addressRepository.save(address).toDTO()
    }

    @Transactional
    fun deleteAddress(firebaseUid: String, addressId: UUID) {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val address = addressRepository.findById(addressId).orElseThrow { ResourceNotFoundException("Address not found") }
        
        if (address.user.id != user.id) throw ResourceNotFoundException("Address not found for this user")

        addressRepository.delete(address)
    }

    @Transactional
    fun setDefaultAddress(firebaseUid: String, addressId: UUID): AddressDTO {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val userId = user.id ?: throw ResourceNotFoundException("User ID missing")
        val address = addressRepository.findById(addressId).orElseThrow { ResourceNotFoundException("Address not found") }
        
        if (address.user.id != userId) throw ResourceNotFoundException("Address not found for this user")

        // Unset old default
        addressRepository.findByUserIdAndIsDefaultTrue(userId)?.apply {
            this.isDefault = false
            addressRepository.save(this)
        }

        address.isDefault = true
        return addressRepository.save(address).toDTO()
    }

    private fun User.toDTO() = UserProfileDTO(
        id = id ?: UUID.randomUUID(),
        firebaseUid = firebaseUid,
        email = email,
        name = name,
        phoneNumber = phoneNumber,
        profileImageUrl = profileImageUrl,
        fcmToken = fcmToken
    )

    private fun UserAddress.toDTO() = AddressDTO(
        id = id ?: UUID.randomUUID(),
        title = title,
        fullAddress = fullAddress,
        latitude = location.y,
        longitude = location.x,
        isDefault = isDefault
    )
}
