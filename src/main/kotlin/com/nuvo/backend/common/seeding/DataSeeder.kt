package com.nuvo.backend.common.seeding

import com.nuvo.backend.common.util.GeometryUtil
import com.nuvo.backend.features.catalog.domain.*
import com.nuvo.backend.features.catalog.repository.*
import com.nuvo.backend.features.social.domain.*
import com.nuvo.backend.features.social.repository.*
import com.nuvo.backend.features.store.domain.*
import com.nuvo.backend.features.store.repository.*
import com.nuvo.backend.features.user.domain.*
import com.nuvo.backend.features.user.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Component
class DataSeeder(
    private val userRepository: UserRepository,
    private val addressRepository: UserAddressRepository,
    private val chainRepository: ChainRepository,
    private val storeRepository: StoreRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val productRepository: ProductRepository,
    private val skuRepository: SKURepository,
    private val reviewRepository: ReviewRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String) {
        if (userRepository.count() > 0) return

        println("🌱 Starting comprehensive data seeding...")

        // 1. Create Mock Users
        val mockUser = userRepository.save(User(
            firebaseUid = "mock-user-id",
            email = "tester@nuvo.com",
            name = "Nuvo Tester",
            phoneNumber = "+123456789"
        ))

        // 2. Create User Addresses
        addressRepository.save(UserAddress(
            user = mockUser,
            title = "Home",
            fullAddress = "123 Main St, Cape Town",
            location = GeometryUtil.createPoint(-33.9249, 18.4241),
            isDefault = true
        ))

        // 3. Create 5 Chains
        val chains = listOf(
            createChain("Burger King", "Global fast food chain", "https://logo.com/bk.png"),
            createChain("Walmart", "Massive retail chain", "https://logo.com/walmart.png"),
            createChain("Sephora", "Premium cosmetics and beauty", "https://logo.com/sephora.png"),
            createChain("Zara", "Global fashion retailer", "https://logo.com/zara.png"),
            createChain("CVS Pharmacy", "Health and wellness", "https://logo.com/cvs.png")
        )

        // 4. Create Chain Stores and Catalogs
        chains.forEach { chain ->
            val storeCount = Random().nextInt(4) + 2
            repeat(storeCount) { i ->
                val store = createStore("${chain.name} - Store #$i", chain)
                seedCatalogForStore(store, chain.name)
                
                // Add initial reviews for store rating logic
                reviewRepository.save(Review(
                    user = mockUser,
                    store = store,
                    rating = 4 + Random().nextInt(2),
                    comment = "Great experience at ${store.name}!"
                ))
                updateStoreAverageRating(store.id!!)
            }
        }

        // 5. Create Independent Stores
        val independentNames = listOf("The Pizza Place", "Corner Groceries", "Local Pharma", "Style Boutique", "Bakery Delight")
        repeat(15) { i ->
            val store = createStore("${independentNames[i % independentNames.size]} $i", null)
            val industry = when (i % 5) {
                0 -> "Food"; 1 -> "Grocery"; 2 -> "Pharma"; 3 -> "Fashion"; else -> "Cosmetic"
            }
            seedCatalogForStore(store, industry)
        }

        println("✅ Comprehensive seeding completed!")
    }

    private fun createChain(name: String, desc: String, logo: String) = 
        chainRepository.save(Chain(name = name, description = desc, logoUrl = logo))

    private fun createStore(name: String, chain: Chain?): Store {
        val lat = -33.9 + (Random().nextDouble() * 0.1)
        val lng = 18.4 + (Random().nextDouble() * 0.1)
        return storeRepository.save(Store(
            chain = chain,
            name = name,
            description = "Welcome to $name",
            contactNumber = "+123456789",
            location = GeometryUtil.createPoint(lat, lng),
            address = "$name Street, City",
            isActive = true
        ))
    }

    private fun seedCatalogForStore(store: Store, industry: String) {
        when {
            industry.contains("Burger") || industry == "Food" -> seedFood(store)
            industry.contains("Walmart") || industry == "Grocery" -> seedGrocery(store)
            industry.contains("Sephora") || industry == "Cosmetic" -> seedCosmetic(store)
            industry.contains("Zara") || industry == "Fashion" -> seedFashion(store)
            industry.contains("CVS") || industry == "Pharma" -> seedPharma(store)
        }
    }

    private fun seedFood(store: Store) {
        val cat = categoryRepository.save(Category(store = store, name = "Burgers"))
        val sub = subCategoryRepository.save(SubCategory(category = cat, name = "Beef Burgers"))
        val prod = productRepository.save(Product(store = store, subCategory = sub, name = "Whopper", description = "Classic flame-grilled beef"))
        skuRepository.save(SKU(product = prod, name = "Regular", originalPrice = BigDecimal("5.99")))
        skuRepository.save(SKU(product = prod, name = "Double", originalPrice = BigDecimal("8.99")))
    }

    private fun seedGrocery(store: Store) {
        val cat = categoryRepository.save(Category(store = store, name = "Dairy"))
        val sub = subCategoryRepository.save(SubCategory(category = cat, name = "Milk"))
        val prod = productRepository.save(Product(store = store, subCategory = sub, name = "Full Cream Milk"))
        skuRepository.save(SKU(product = prod, name = "1L", originalPrice = BigDecimal("1.50")))
        skuRepository.save(SKU(product = prod, name = "2L", originalPrice = BigDecimal("2.80")))
    }

    private fun seedPharma(store: Store) {
        val cat = categoryRepository.save(Category(store = store, name = "Wellness"))
        val sub = subCategoryRepository.save(SubCategory(category = cat, name = "Vitamins"))
        val prod = productRepository.save(Product(store = store, subCategory = sub, name = "Vitamin C"))
        skuRepository.save(SKU(product = prod, name = "30 Tablets", originalPrice = BigDecimal("12.00")))
        skuRepository.save(SKU(product = prod, name = "90 Tablets", originalPrice = BigDecimal("25.00")))
    }

    private fun seedFashion(store: Store) {
        val cat = categoryRepository.save(Category(store = store, name = "Menswear"))
        val sub = subCategoryRepository.save(SubCategory(category = cat, name = "Shirts"))
        val prod = productRepository.save(Product(store = store, subCategory = sub, name = "Slim Fit Shirt"))
        skuRepository.save(SKU(product = prod, name = "Small", originalPrice = BigDecimal("29.99")))
        skuRepository.save(SKU(product = prod, name = "Large", originalPrice = BigDecimal("29.99")))
    }

    private fun seedCosmetic(store: Store) {
        val cat = categoryRepository.save(Category(store = store, name = "Skincare"))
        val sub = subCategoryRepository.save(SubCategory(category = cat, name = "Moisturizers"))
        val prod = productRepository.save(Product(store = store, subCategory = sub, name = "Face Cream"))
        skuRepository.save(SKU(product = prod, name = "50ml", originalPrice = BigDecimal("45.00")))
        skuRepository.save(SKU(product = prod, name = "100ml", originalPrice = BigDecimal("75.00")))
    }

    private fun updateStoreAverageRating(storeId: UUID) {
        val store = storeRepository.findById(storeId).orElseThrow()
        val reviews = reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId)
        if (reviews.isNotEmpty()) {
            store.averageRating = reviews.map { it.rating }.average()
            storeRepository.save(store)
        }
    }
}
