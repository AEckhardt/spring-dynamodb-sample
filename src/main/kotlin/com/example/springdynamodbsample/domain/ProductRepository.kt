package com.example.springdynamodbsample.domain

interface ProductRepository {
    fun save(product: Product)
    fun find(id: String): Product?
    fun delete(id: String): Product?
    fun findAll(): List<Product>
    fun findAllByCategory(category: String): List<Product>
    fun findAllByBrand(brand: String): List<Product>
}