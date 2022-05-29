package com.example.springdynamodbsample.infrastructure.dynamodb

import com.example.springdynamodbsample.domain.Product
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class ProductAttributeConverter : AttributeConverter<Product> {

    private val objectMapper: ObjectMapper = ObjectMapper().registerModules(
        KotlinModule.Builder().build(),
        JavaTimeModule(),
        Jdk8Module()
    ).setSerializationInclusion(JsonInclude.Include.NON_NULL)

    override fun transformFrom(input: Product?): AttributeValue {
        return AttributeValue.fromS(objectMapper.writeValueAsString(input))
    }

    override fun transformTo(input: AttributeValue?): Product {
        return objectMapper.readValue(input?.s(), Product::class.java)
    }

    override fun type(): EnhancedType<Product> {
        return EnhancedType.of(Product::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}