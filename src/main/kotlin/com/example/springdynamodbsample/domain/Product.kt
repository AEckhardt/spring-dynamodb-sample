package com.example.springdynamodbsample.domain

import java.time.LocalDateTime

data class Product(
    val id: String,
    val name: String,
    val brand: String?,
    val category: String,
    val description: String?,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)
