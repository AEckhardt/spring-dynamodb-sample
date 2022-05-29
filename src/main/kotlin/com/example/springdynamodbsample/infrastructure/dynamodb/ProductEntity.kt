package com.example.springdynamodbsample.infrastructure.dynamodb

import com.example.springdynamodbsample.domain.Product
import com.example.springdynamodbsample.infrastructure.dynamodb.ProductDynamoDbAdapter.Companion
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey

@DynamoDbBean
data class ProductEntity(
    @get:DynamoDbPartitionKey
    @get:DynamoDbSecondarySortKey(indexNames = [ProductDynamoDbAdapter.BRAND_INDEX, ProductDynamoDbAdapter.CATEGORY_INDEX])
    var id: String? = null,

    @get:DynamoDbSecondaryPartitionKey(indexNames = [ProductDynamoDbAdapter.BRAND_INDEX])
    var brand: String? = null,
    @get:DynamoDbSecondaryPartitionKey(indexNames = [ProductDynamoDbAdapter.CATEGORY_INDEX])
    var category: String? = null,

    @get:DynamoDbConvertedBy(ProductAttributeConverter::class)
    var product: Product? = null
)