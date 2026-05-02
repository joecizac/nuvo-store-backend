package com.nuvo.backend.features.store.service

import com.nuvo.backend.TestFixtures
import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.util.GeometryUtil
import com.nuvo.backend.features.store.repository.ChainRepository
import com.nuvo.backend.features.store.repository.StoreRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class StoreServiceTests {
    private lateinit var storeRepository: StoreRepository
    private lateinit var chainRepository: ChainRepository
    private lateinit var service: StoreService

    @BeforeEach
    fun setUp() {
        storeRepository = Mockito.mock(StoreRepository::class.java)
        chainRepository = Mockito.mock(ChainRepository::class.java)
        service = StoreService(storeRepository, chainRepository)
    }

    @Test
    fun `getNearbyStoresFiltered delegates to native repository and maps results`() {
        val store = TestFixtures.store()
        val pageable = PageRequest.of(0, 5)
        val point = GeometryUtil.createPoint(-33.9, 18.4)
        `when`(storeRepository.findNearbyStoresFiltered(
            point,
            1000.0,
            "grocery",
            2,
            true,
            pageable
        )).thenReturn(PageImpl(listOf(store), pageable, 1))

        val result = service.getNearbyStoresFiltered(-33.9, 18.4, 1000.0, "grocery", 2, true, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(store.id, result.content.single().id)
        assertEquals(store.location.y, result.content.single().latitude)
        assertEquals(store.location.x, result.content.single().longitude)
    }

    @Test
    fun `getStore maps store details and fails when missing`() {
        val store = TestFixtures.store(chain = TestFixtures.chain())
        `when`(storeRepository.findById(store.id!!)).thenReturn(Optional.of(store))
        `when`(storeRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.empty())

        val dto = service.getStore(store.id!!)

        assertEquals(store.id, dto.id)
        assertEquals(store.chain?.id, dto.chainId)
        assertEquals(store.deliveryFee, dto.deliveryFee)
        assertThrows<ResourceNotFoundException> {
            service.getStore(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        }
    }

    @Test
    fun `getAllChains and getStoresByChain map repository results`() {
        val chain = TestFixtures.chain()
        val store = TestFixtures.store(chain = chain)
        `when`(chainRepository.findAll()).thenReturn(listOf(chain))
        `when`(storeRepository.findAllByChainId(chain.id!!)).thenReturn(listOf(store))

        assertEquals(chain.id, service.getAllChains().single().id)
        assertEquals(store.id, service.getStoresByChain(chain.id!!).single().id)
    }
}
