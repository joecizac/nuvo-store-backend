package com.nuvo.backend.features.user.service

import com.nuvo.backend.TestFixtures
import com.nuvo.backend.TestFixtures.any
import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.exception.ValidationException
import com.nuvo.backend.features.user.dto.AddressDTO
import com.nuvo.backend.features.user.dto.SyncUserRequest
import com.nuvo.backend.features.user.repository.UserAddressRepository
import com.nuvo.backend.features.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserServiceTests {
    private lateinit var userRepository: UserRepository
    private lateinit var addressRepository: UserAddressRepository
    private lateinit var service: UserService

    private val firebaseUid = "firebase-user"
    private val user = TestFixtures.user(firebaseUid = firebaseUid)

    @BeforeEach
    fun setUp() {
        userRepository = Mockito.mock(UserRepository::class.java)
        addressRepository = Mockito.mock(UserAddressRepository::class.java)
        service = UserService(userRepository, addressRepository)
    }

    @Test
    fun `syncUser creates a new profile`() {
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(null)
        `when`(userRepository.save(any())).thenAnswer {
            val input = it.arguments[0] as com.nuvo.backend.features.user.domain.User
            com.nuvo.backend.features.user.domain.User(
                id = UUID.randomUUID(),
                firebaseUid = input.firebaseUid,
                email = input.email,
                name = input.name,
                phoneNumber = input.phoneNumber,
                profileImageUrl = input.profileImageUrl,
                fcmToken = input.fcmToken
            )
        }

        val dto = service.syncUser(firebaseUid, SyncUserRequest("New User", "new@example.com", "avatar.png"))

        assertEquals(firebaseUid, dto.firebaseUid)
        assertEquals("New User", dto.name)
        assertEquals("new@example.com", dto.email)
        assertEquals("avatar.png", dto.profileImageUrl)
        verify(userRepository).save(any())
    }

    @Test
    fun `syncUser updates an existing profile without clearing existing image when omitted`() {
        user.profileImageUrl = "old.png"
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)

        val dto = service.syncUser(firebaseUid, SyncUserRequest("Updated", "updated@example.com"))

        assertEquals("Updated", dto.name)
        assertEquals("updated@example.com", dto.email)
        assertEquals("old.png", dto.profileImageUrl)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `addAddress unsets previous default when adding a new default`() {
        val previousDefault = TestFixtures.address(user = user, isDefault = true)
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(addressRepository.findByUserIdAndIsDefaultTrue(user.id!!)).thenReturn(previousDefault)
        `when`(addressRepository.save(any())).thenAnswer {
            val input = it.arguments[0] as com.nuvo.backend.features.user.domain.UserAddress
            if (input.id != null) input else com.nuvo.backend.features.user.domain.UserAddress(
                id = UUID.randomUUID(),
                user = input.user,
                title = input.title,
                fullAddress = input.fullAddress,
                location = input.location,
                isDefault = input.isDefault
            )
        }

        val dto = service.addAddress(firebaseUid, addressDto(isDefault = true))

        assertTrue(dto.isDefault)
        assertFalse(previousDefault.isDefault)
        verify(addressRepository).save(previousDefault)
    }

    @Test
    fun `updateAddress rejects an address owned by another user`() {
        val otherAddress = TestFixtures.address(user = TestFixtures.user(firebaseUid = "other"))
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(addressRepository.findById(otherAddress.id!!)).thenReturn(Optional.of(otherAddress))

        assertThrows<ResourceNotFoundException> {
            service.updateAddress(firebaseUid, otherAddress.id!!, addressDto())
        }
    }

    @Test
    fun `deleteAddress rejects default address`() {
        val defaultAddress = TestFixtures.address(user = user, isDefault = true)
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(addressRepository.findById(defaultAddress.id!!)).thenReturn(Optional.of(defaultAddress))

        assertThrows<ValidationException> {
            service.deleteAddress(firebaseUid, defaultAddress.id!!)
        }

        verify(addressRepository, never()).delete(defaultAddress)
    }

    @Test
    fun `setDefaultAddress unsets previous default and marks target default`() {
        val previousDefault = TestFixtures.address(user = user, isDefault = true)
        val target = TestFixtures.address(user = user, isDefault = false)
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(addressRepository.findById(target.id!!)).thenReturn(Optional.of(target))
        `when`(addressRepository.findByUserIdAndIsDefaultTrue(user.id!!)).thenReturn(previousDefault)
        `when`(addressRepository.save(any())).thenAnswer { it.arguments[0] }

        val dto = service.setDefaultAddress(firebaseUid, target.id!!)

        assertFalse(previousDefault.isDefault)
        assertTrue(target.isDefault)
        assertTrue(dto.isDefault)
    }

    @Test
    fun `getUserProfile fails for unknown user`() {
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(null)

        assertThrows<ResourceNotFoundException> {
            service.getUserProfile(firebaseUid)
        }
    }

    private fun addressDto(isDefault: Boolean = false) = AddressDTO(
        title = "Office",
        fullAddress = "456 Test Avenue",
        latitude = -34.0,
        longitude = 18.5,
        isDefault = isDefault
    )
}
