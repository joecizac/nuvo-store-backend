package com.nuvo.backend.features.catalog.service

import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.features.catalog.domain.Category
import com.nuvo.backend.features.catalog.domain.Product
import com.nuvo.backend.features.catalog.domain.SKU
import com.nuvo.backend.features.catalog.domain.SubCategory
import com.nuvo.backend.features.catalog.dto.CategoryDTO
import com.nuvo.backend.features.catalog.dto.ProductDTO
import com.nuvo.backend.features.catalog.dto.SkuDTO
import com.nuvo.backend.features.catalog.dto.SubCategoryDTO
import com.nuvo.backend.features.catalog.repository.CategoryRepository
import com.nuvo.backend.features.catalog.repository.ProductRepository
import com.nuvo.backend.features.catalog.repository.SubCategoryRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CatalogService(
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val productRepository: ProductRepository
) {

    @Transactional(readOnly = true)
    @Cacheable(value = ["store_categories"], key = "#storeId")
    fun getStoreCategories(storeId: UUID): List<CategoryDTO> {
        return categoryRepository.findAllByStoreId(storeId).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["sub_categories"], key = "#categoryId")
    fun getSubCategories(categoryId: UUID): List<SubCategoryDTO> {
        return subCategoryRepository.findAllByCategoryId(categoryId).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["store_products"], key = "#storeId + '-' + #subCategoryId + '-' + #pageable.pageNumber")
    fun getStoreProducts(
        storeId: UUID,
        subCategoryId: UUID?,
        pageable: Pageable
    ): Page<ProductDTO> {
        val products = if (subCategoryId != null) {
            productRepository.findAllByStoreIdAndSubCategoryId(storeId, subCategoryId, pageable)
        } else {
            productRepository.findAllByStoreId(storeId, pageable)
        }
        return products.map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["product_details"], key = "#productId")
    fun getProduct(productId: UUID): ProductDTO {
        return productRepository.findById(productId)
            .orElseThrow { ResourceNotFoundException("Product not found") }
            .toDTO()
    }

    private fun Category.toDTO() = CategoryDTO(id ?: UUID.randomUUID(), name, imageUrl)

    private fun SubCategory.toDTO() = SubCategoryDTO(id ?: UUID.randomUUID(), category.id ?: UUID.randomUUID(), name)

    private fun Product.toDTO() = ProductDTO(
        id = id ?: UUID.randomUUID(),
        storeId = store.id ?: UUID.randomUUID(),
        subCategoryId = subCategory.id ?: UUID.randomUUID(),
        name = name,
        description = description,
        imageUrl = imageUrl,
        isAvailable = isAvailable,
        skus = skus.map { it.toDTO() }
    )

    private fun SKU.toDTO() = SkuDTO(
        id = id ?: UUID.randomUUID(),
        productId = product.id ?: UUID.randomUUID(),
        name = name,
        imageUrl = imageUrl,
        originalPrice = originalPrice,
        discountedPrice = discountedPrice,
        isAvailable = isAvailable
    )
}
