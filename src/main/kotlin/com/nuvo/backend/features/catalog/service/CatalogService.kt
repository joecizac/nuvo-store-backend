package com.nuvo.backend.features.catalog.service

import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.features.catalog.domain.Category
import com.nuvo.backend.features.catalog.domain.Product
import com.nuvo.backend.features.catalog.domain.ProductStatus
import com.nuvo.backend.features.catalog.domain.SKU
import com.nuvo.backend.features.catalog.domain.SubCategory
import com.nuvo.backend.features.catalog.dto.CategoryDTO
import com.nuvo.backend.features.catalog.dto.ProductDTO
import com.nuvo.backend.features.catalog.dto.ProductPriceSummaryDTO
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
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Service
class CatalogService(
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val productRepository: ProductRepository
) {
    private fun UUID?.required(fieldName: String): UUID =
        this ?: throw IllegalStateException("Missing UUID for $fieldName")

    private fun BigDecimal.toCents(): Long =
        multiply(BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValueExact()

    private fun Product.availableSkus(): List<SKU> = skus.filter { it.isAvailable }

    private fun Product.priceSummary(): ProductPriceSummaryDTO {
        val prices = availableSkus().map { (it.discountedPrice ?: it.originalPrice).toCents() }
        if (prices.isEmpty()) {
            throw IllegalStateException("Product ${id.required("product.id")} has no available SKUs")
        }
        val minPrice = prices.min()
        val maxPrice = prices.max()
        return ProductPriceSummaryDTO(
            minPrice = minPrice,
            maxPrice = maxPrice,
            displayPrice = minPrice,
            hasPriceRange = minPrice != maxPrice
        )
    }


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
            productRepository.findPublicByStoreIdAndSubCategoryId(storeId, subCategoryId, ProductStatus.ACTIVE, pageable)
        } else {
            productRepository.findPublicByStoreId(storeId, ProductStatus.ACTIVE, pageable)
        }
        return products.map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["product_details"], key = "#productId")
    fun getProduct(productId: UUID): ProductDTO {
        return productRepository.findPublicById(productId, ProductStatus.ACTIVE)
            .orElseThrow { ResourceNotFoundException("Product not found") }
            .toDTO()
    }

    private fun Category.toDTO() = CategoryDTO(id.required("category.id"), name, imageUrl)

    private fun SubCategory.toDTO() = SubCategoryDTO(
        id.required("subCategory.id"),
        category.id.required("subCategory.category.id"),
        name
    )

    private fun Product.toDTO() = ProductDTO(
        id = id.required("product.id"),
        storeId = store.id.required("product.store.id"),
        subCategoryId = subCategory.id.required("product.subCategory.id"),
        categoryId = subCategory.category.id.required("product.subCategory.category.id"),
        name = name,
        description = description,
        priceSummary = priceSummary(),
        imageUrl = imageUrl,
        isAvailable = isAvailable,
        rating = 0.0,
        isFavourite = false,
        skus = availableSkus().map { it.toDTO() }
    )

    private fun SKU.toDTO() = SkuDTO(
        id = id.required("sku.id"),
        productId = product.id.required("sku.product.id"),
        name = name,
        imageUrl = imageUrl,
        originalPrice = originalPrice,
        discountedPrice = discountedPrice,
        isAvailable = isAvailable
    )
}
