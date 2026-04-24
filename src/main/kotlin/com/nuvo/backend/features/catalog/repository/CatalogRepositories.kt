package com.nuvo.backend.features.catalog.repository

import com.nuvo.backend.features.catalog.domain.Category
import com.nuvo.backend.features.catalog.domain.SubCategory
import com.nuvo.backend.features.catalog.domain.Product
import com.nuvo.backend.features.catalog.domain.SKU
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findAllByStoreId(storeId: UUID): List<Category>
}

interface SubCategoryRepository : JpaRepository<SubCategory, UUID> {
    fun findAllByCategoryId(categoryId: UUID): List<SubCategory>
}

interface ProductRepository : JpaRepository<Product, UUID> {
    @EntityGraph(attributePaths = ["skus"])
    fun findAllByStoreIdAndSubCategoryId(storeId: UUID, subCategoryId: UUID, pageable: Pageable): Page<Product>

    @EntityGraph(attributePaths = ["skus"])
    fun findAllByStoreId(storeId: UUID, pageable: Pageable): Page<Product>

    @EntityGraph(attributePaths = ["skus"])
    override fun findById(id: UUID): Optional<Product>
}

interface SKURepository : JpaRepository<SKU, UUID> {
    fun findAllByProductId(productId: UUID): List<SKU>
}
