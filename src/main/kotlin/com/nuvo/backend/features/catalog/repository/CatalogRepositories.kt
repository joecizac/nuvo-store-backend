package com.nuvo.backend.features.catalog.repository

import com.nuvo.backend.features.catalog.domain.Category
import com.nuvo.backend.features.catalog.domain.SubCategory
import com.nuvo.backend.features.catalog.domain.Product
import com.nuvo.backend.features.catalog.domain.ProductStatus
import com.nuvo.backend.features.catalog.domain.SKU
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findAllByStoreId(storeId: UUID): List<Category>
}

interface SubCategoryRepository : JpaRepository<SubCategory, UUID> {
    fun findAllByCategoryId(categoryId: UUID): List<SubCategory>
}

interface ProductRepository : JpaRepository<Product, UUID> {
    @EntityGraph(attributePaths = ["skus"])
    @Query(
        """
        select distinct p from Product p
        join p.skus s
        where p.store.id = :storeId
          and p.subCategory.id = :subCategoryId
          and p.status = :status
          and p.isAvailable = true
          and s.isAvailable = true
        """
    )
    fun findPublicByStoreIdAndSubCategoryId(
        @Param("storeId") storeId: UUID,
        @Param("subCategoryId") subCategoryId: UUID,
        @Param("status") status: ProductStatus,
        pageable: Pageable
    ): Page<Product>

    @EntityGraph(attributePaths = ["skus"])
    @Query(
        """
        select distinct p from Product p
        join p.skus s
        where p.store.id = :storeId
          and p.status = :status
          and p.isAvailable = true
          and s.isAvailable = true
        """
    )
    fun findPublicByStoreId(
        @Param("storeId") storeId: UUID,
        @Param("status") status: ProductStatus,
        pageable: Pageable
    ): Page<Product>

    @EntityGraph(attributePaths = ["skus"])
    @Query(
        """
        select distinct p from Product p
        join p.skus s
        where p.id = :id
          and p.status = :status
          and p.isAvailable = true
          and s.isAvailable = true
        """
    )
    fun findPublicById(
        @Param("id") id: UUID,
        @Param("status") status: ProductStatus
    ): Optional<Product>

    @EntityGraph(attributePaths = ["skus"])
    override fun findById(id: UUID): Optional<Product>
}

interface SKURepository : JpaRepository<SKU, UUID> {
    fun findAllByProductId(productId: UUID): List<SKU>
}
