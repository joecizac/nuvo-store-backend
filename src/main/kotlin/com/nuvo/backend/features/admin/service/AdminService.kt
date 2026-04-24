package com.nuvo.backend.features.admin.service

import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.util.GeometryUtil
import com.nuvo.backend.features.admin.dto.*
import com.nuvo.backend.features.catalog.domain.*
import com.nuvo.backend.features.catalog.repository.*
import com.nuvo.backend.features.store.domain.*
import com.nuvo.backend.features.store.repository.*
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdminService(
    private val chainRepository: ChainRepository,
    private val storeRepository: StoreRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val productRepository: ProductRepository,
    private val skuRepository: SKURepository
) {

    @Transactional
    @CacheEvict(value = ["global_chains"], allEntries = true)
    fun createChain(request: AdminChainRequest): Chain {
        return chainRepository.save(
            Chain(
                name = request.name,
                description = request.description,
                logoUrl = request.logoUrl,
                bannerUrl = request.bannerUrl
            )
        )
    }

    @Transactional
    @CacheEvict(value = ["nearby_stores", "store_details", "chain_stores"], allEntries = true)
    fun createStore(request: AdminStoreRequest): Store {
        val chain = request.chainId?.let {
            chainRepository.findById(it).orElseThrow { ResourceNotFoundException("Chain not found") }
        }
        return storeRepository.save(
            Store(
                chain = chain,
                name = request.name,
                description = request.description,
                contactNumber = request.contactNumber,
                logoUrl = request.logoUrl,
                bannerUrl = request.bannerUrl,
                location = GeometryUtil.createPoint(request.latitude, request.longitude),
                address = request.address,
                isActive = request.isActive
            )
        )
    }

    @Transactional
    @CacheEvict(value = ["store_categories"], key = "#storeId")
    fun createCategory(storeId: UUID, request: AdminCategoryRequest): Category {
        val store = storeRepository.findById(storeId).orElseThrow { ResourceNotFoundException("Store not found") }
        return categoryRepository.save(
            Category(
                store = store,
                name = request.name,
                imageUrl = request.imageUrl
            )
        )
    }

    @Transactional
    @CacheEvict(value = ["sub_categories"], key = "#categoryId")
    fun createSubCategory(categoryId: UUID, request: AdminSubCategoryRequest): SubCategory {
        val category = categoryRepository.findById(categoryId).orElseThrow { ResourceNotFoundException("Category not found") }
        return subCategoryRepository.save(
            SubCategory(
                category = category,
                name = request.name
            )
        )
    }

    @Transactional
    @CacheEvict(value = ["store_products"], allEntries = true)
    fun createProduct(storeId: UUID, request: AdminProductRequest): Product {
        val store = storeRepository.findById(storeId).orElseThrow { ResourceNotFoundException("Store not found") }
        val subCategory = subCategoryRepository.findById(request.subCategoryId).orElseThrow { ResourceNotFoundException("SubCategory not found") }
        
        return productRepository.save(
            Product(
                store = store,
                subCategory = subCategory,
                name = request.name,
                description = request.description,
                imageUrl = request.imageUrl,
                isAvailable = request.isAvailable
            )
        )
    }

    @Transactional
    @CacheEvict(value = ["product_details"], key = "#productId")
    fun createSku(productId: UUID, request: AdminSkuRequest): SKU {
        val product = productRepository.findById(productId).orElseThrow { ResourceNotFoundException("Product not found") }
        return skuRepository.save(
            SKU(
                product = product,
                name = request.name,
                imageUrl = request.imageUrl,
                originalPrice = request.originalPrice,
                discountedPrice = request.discountedPrice,
                isAvailable = request.isAvailable
            )
        )
    }
}
