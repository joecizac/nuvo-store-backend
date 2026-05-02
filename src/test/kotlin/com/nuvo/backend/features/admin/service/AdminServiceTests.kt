package com.nuvo.backend.features.admin.service

import com.nuvo.backend.TestFixtures
import com.nuvo.backend.TestFixtures.any
import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.features.admin.dto.AdminCategoryRequest
import com.nuvo.backend.features.admin.dto.AdminChainRequest
import com.nuvo.backend.features.admin.dto.AdminProductRequest
import com.nuvo.backend.features.admin.dto.AdminSkuRequest
import com.nuvo.backend.features.admin.dto.AdminStoreRequest
import com.nuvo.backend.features.admin.dto.AdminSubCategoryRequest
import com.nuvo.backend.features.catalog.repository.CategoryRepository
import com.nuvo.backend.features.catalog.repository.ProductRepository
import com.nuvo.backend.features.catalog.repository.SKURepository
import com.nuvo.backend.features.catalog.repository.SubCategoryRepository
import com.nuvo.backend.features.store.repository.ChainRepository
import com.nuvo.backend.features.store.repository.StoreRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class AdminServiceTests {
    private lateinit var chainRepository: ChainRepository
    private lateinit var storeRepository: StoreRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var subCategoryRepository: SubCategoryRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var skuRepository: SKURepository
    private lateinit var service: AdminService

    @BeforeEach
    fun setUp() {
        chainRepository = Mockito.mock(ChainRepository::class.java)
        storeRepository = Mockito.mock(StoreRepository::class.java)
        categoryRepository = Mockito.mock(CategoryRepository::class.java)
        subCategoryRepository = Mockito.mock(SubCategoryRepository::class.java)
        productRepository = Mockito.mock(ProductRepository::class.java)
        skuRepository = Mockito.mock(SKURepository::class.java)
        service = AdminService(chainRepository, storeRepository, categoryRepository, subCategoryRepository, productRepository, skuRepository)
    }

    @Test
    fun `createChain persists requested fields`() {
        `when`(chainRepository.save(any())).thenAnswer { it.arguments[0] }

        val chain = service.createChain(AdminChainRequest("Chain", "Description", "logo", "banner"))

        assertEquals("Chain", chain.name)
        assertEquals("Description", chain.description)
        verify(chainRepository).save(any())
    }

    @Test
    fun `createStore links an existing chain`() {
        val chain = TestFixtures.chain()
        `when`(chainRepository.findById(chain.id!!)).thenReturn(Optional.of(chain))
        `when`(storeRepository.save(any())).thenAnswer { it.arguments[0] }

        val store = service.createStore(
            AdminStoreRequest(chain.id, "Store", "Description", "+1", "logo", "banner", -33.9, 18.4, "Address")
        )

        assertEquals(chain, store.chain)
        assertEquals("Store", store.name)
        assertEquals(-33.9, store.location.y)
        assertEquals(18.4, store.location.x)
    }

    @Test
    fun `createStore fails when chain is missing`() {
        val chainId = UUID.randomUUID()
        `when`(chainRepository.findById(chainId)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            service.createStore(AdminStoreRequest(chainId, "Store", null, null, null, null, -33.9, 18.4, "Address"))
        }
    }

    @Test
    fun `createCategory createSubCategory createProduct and createSku persist hierarchy`() {
        val store = TestFixtures.store()
        val category = TestFixtures.category(store = store)
        val subCategory = TestFixtures.subCategory(category = category)
        val product = TestFixtures.product(store = store, subCategory = subCategory)

        `when`(storeRepository.findById(store.id!!)).thenReturn(Optional.of(store))
        `when`(categoryRepository.findById(category.id!!)).thenReturn(Optional.of(category))
        `when`(subCategoryRepository.findById(subCategory.id!!)).thenReturn(Optional.of(subCategory))
        `when`(productRepository.findById(product.id!!)).thenReturn(Optional.of(product))
        `when`(categoryRepository.save(any())).thenAnswer { it.arguments[0] }
        `when`(subCategoryRepository.save(any())).thenAnswer { it.arguments[0] }
        `when`(productRepository.save(any())).thenAnswer { it.arguments[0] }
        `when`(skuRepository.save(any())).thenAnswer { it.arguments[0] }

        val savedCategory = service.createCategory(store.id!!, AdminCategoryRequest("Food", "food.png"))
        val savedSubCategory = service.createSubCategory(category.id!!, AdminSubCategoryRequest("Pizza"))
        val savedProduct = service.createProduct(store.id!!, AdminProductRequest(subCategory.id!!, "Margherita", "Classic", "pizza.png"))
        val savedSku = service.createSku(product.id!!, AdminSkuRequest("Large", "large.png", BigDecimal("20.00"), BigDecimal("18.00")))

        assertEquals(store, savedCategory.store)
        assertEquals(category, savedSubCategory.category)
        assertEquals(subCategory, savedProduct.subCategory)
        assertEquals(product, savedSku.product)
    }
}
