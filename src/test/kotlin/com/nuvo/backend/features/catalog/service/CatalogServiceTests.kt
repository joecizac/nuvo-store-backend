package com.nuvo.backend.features.catalog.service

import com.nuvo.backend.TestFixtures
import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.features.catalog.domain.ProductStatus
import com.nuvo.backend.features.catalog.repository.CategoryRepository
import com.nuvo.backend.features.catalog.repository.ProductRepository
import com.nuvo.backend.features.catalog.repository.SubCategoryRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class CatalogServiceTests {
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var subCategoryRepository: SubCategoryRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var service: CatalogService

    @BeforeEach
    fun setUp() {
        categoryRepository = Mockito.mock(CategoryRepository::class.java)
        subCategoryRepository = Mockito.mock(SubCategoryRepository::class.java)
        productRepository = Mockito.mock(ProductRepository::class.java)
        service = CatalogService(categoryRepository, subCategoryRepository, productRepository)
    }

    @Test
    fun `getStoreCategories maps categories`() {
        val store = TestFixtures.store()
        val category = TestFixtures.category(store = store)
        `when`(categoryRepository.findAllByStoreId(store.id!!)).thenReturn(listOf(category))

        val result = service.getStoreCategories(store.id!!)

        assertEquals(1, result.size)
        assertEquals(category.id, result.single().id)
        assertEquals(category.name, result.single().name)
    }

    @Test
    fun `getStoreProducts filters by subcategory and includes skus`() {
        val store = TestFixtures.store()
        val subCategory = TestFixtures.subCategory(category = TestFixtures.category(store = store))
        val product = TestFixtures.product(store = store, subCategory = subCategory)
        product.status = ProductStatus.ACTIVE
        product.skus.add(TestFixtures.sku(product = product, originalPrice = "12.00", discountedPrice = "9.50"))
        val pageable = PageRequest.of(0, 10)

        `when`(productRepository.findPublicByStoreIdAndSubCategoryId(store.id!!, subCategory.id!!, ProductStatus.ACTIVE, pageable))
            .thenReturn(PageImpl(listOf(product), pageable, 1))

        val result = service.getStoreProducts(store.id!!, subCategory.id!!, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(product.id, result.content.single().id)
        assertEquals(subCategory.id, result.content.single().subCategoryId)
        assertEquals(BigDecimal("9.50"), result.content.single().skus.single().discountedPrice)
        assertEquals(950, result.content.single().priceSummary.displayPrice)
        assertEquals(false, result.content.single().priceSummary.hasPriceRange)
    }

    @Test
    fun `getStoreProducts calculates price range from available SKUs only`() {
        val store = TestFixtures.store()
        val product = TestFixtures.product(store = store)
        product.status = ProductStatus.ACTIVE
        product.skus.add(TestFixtures.sku(product = product, originalPrice = "12.00"))
        product.skus.add(TestFixtures.sku(product = product, originalPrice = "20.00"))
        product.skus.add(TestFixtures.sku(product = product, originalPrice = "5.00").apply { isAvailable = false })
        val pageable = PageRequest.of(0, 10)

        `when`(productRepository.findPublicByStoreId(store.id!!, ProductStatus.ACTIVE, pageable))
            .thenReturn(PageImpl(listOf(product), pageable, 1))

        val result = service.getStoreProducts(store.id!!, null, pageable)

        val summary = result.content.single().priceSummary
        assertEquals(1200, summary.minPrice)
        assertEquals(2000, summary.maxPrice)
        assertEquals(1200, summary.displayPrice)
        assertEquals(true, summary.hasPriceRange)
        assertEquals(2, result.content.single().skus.size)
    }

    @Test
    fun `getProduct uses public active product lookup`() {
        val product = TestFixtures.product()
        product.status = ProductStatus.ACTIVE
        product.skus.add(TestFixtures.sku(product = product, originalPrice = "12.00", discountedPrice = "9.50"))

        `when`(productRepository.findPublicById(product.id!!, ProductStatus.ACTIVE)).thenReturn(Optional.of(product))

        val result = service.getProduct(product.id!!)

        assertEquals(product.id, result.id)
        assertEquals(BigDecimal("9.50"), result.skus.single().discountedPrice)
        assertEquals(950, result.priceSummary.displayPrice)
    }

    @Test
    fun `getProduct fails when product is missing`() {
        val productId = UUID.randomUUID()
        `when`(productRepository.findPublicById(productId, ProductStatus.ACTIVE)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            service.getProduct(productId)
        }
    }
}
