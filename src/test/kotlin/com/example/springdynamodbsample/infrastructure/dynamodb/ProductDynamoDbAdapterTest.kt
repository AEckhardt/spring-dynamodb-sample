package com.example.springdynamodbsample.infrastructure.dynamodb

import com.example.springdynamodbsample.domain.Product
import com.example.springdynamodbsample.infrastructure.dynamodb.ProductDynamoDbAdapter.Companion.TABLE_NAME
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.TableStatus

internal class ProductDynamoDbAdapterTest {

    private val objectMapper: ObjectMapper = ObjectMapper().registerModules(
        KotlinModule.Builder().build(),
        JavaTimeModule(),
        Jdk8Module()
    ).setSerializationInclusion(JsonInclude.Include.NON_NULL)

    companion object {
        private lateinit var dbbClient: DynamoDbClient
        private lateinit var adapter: ProductDynamoDbAdapter
        private lateinit var dbbEnhancedClient: DynamoDbEnhancedClient

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            val container =
                LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3")).withServices(DYNAMODB)
            container.start()
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    if (container.isRunning) {
                        container.stop()
                    }
                }
            )
            dbbClient = DynamoDbClient.builder()
                .endpointOverride(container.getEndpointOverride(DYNAMODB))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(container.accessKey, container.secretKey)
                    )
                )
                .build()
            dbbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dbbClient).build()
            adapter = ProductDynamoDbAdapter(dbbClient)
            adapter.createTableIfNotExists()
        }
    }

    @BeforeEach
    fun deleteAllItems() {
        dbbClient.deleteItem {
            it.tableName(TABLE_NAME).key(
                mutableMapOf("id" to AttributeValue.fromS(product.id))
            )
        }
        dbbClient.deleteItem {
            it.tableName(TABLE_NAME).key(
                mutableMapOf("id" to AttributeValue.fromS(product2.id))
            )
        }
        dbbClient.deleteItem {
            it.tableName(TABLE_NAME).key(
                mutableMapOf("id" to AttributeValue.fromS(product3.id))
            )
        }
    }

    @Test
    fun `should create table`() {
        val describeTable = dbbClient.describeTable { r -> r.tableName(TABLE_NAME) }
        assertThat(describeTable.table().tableStatus()).isEqualTo(TableStatus.ACTIVE)
        assertThat(describeTable.table().hasGlobalSecondaryIndexes()).isTrue
    }

    @Test
    fun `should save product`() {
        adapter.save(product)
        val result =
            dbbClient.getItem { r -> r.tableName(TABLE_NAME).key(mapOf("id" to AttributeValue.fromS(product.id))) }
        assertThat(result.hasItem())
        assertThat(result.item()).isEqualTo(product.inDynamoDB())
    }

    @Test
    fun `should find product`() {
        dbbClient.putItem { r -> r.tableName(TABLE_NAME).item(product.inDynamoDB()) }
        val result = adapter.find(product.id)
        assertThat(result).isEqualTo(product)
    }

    @Test
    fun `should return null if product not found`() {
        val result = adapter.find(product.id)
        assertThat(result).isNull()
    }

    @Test
    fun `should delete product`() {
        dbbClient.putItem { r -> r.tableName(TABLE_NAME).item(product.inDynamoDB()) }
        val result = adapter.delete(product.id)
        assertThat(result).isEqualTo(product)
    }

    @Test
    fun `should return null if product did not exist`() {
        val result = adapter.delete(product.id)
        assertThat(result).isNull()
    }

    @Test
    fun `should return all products of a brand`() {
        dbbClient.putItem { r -> r.tableName(TABLE_NAME).item(product.inDynamoDB()) }
        dbbClient.putItem { r -> r.tableName(TABLE_NAME).item(product2.inDynamoDB()) }
        dbbClient.putItem { r -> r.tableName(TABLE_NAME).item(product3.inDynamoDB()) }

        val result = adapter.findAllByBrand("Milka")
        assertThat(result).containsExactly(product2)
    }

    @Test
    fun `should return all products of a category`() {
        dbbClient.putItem { r -> r.tableName(TABLE_NAME).item(product.inDynamoDB()) }
        dbbClient.putItem { r -> r.tableName(TABLE_NAME).item(product2.inDynamoDB()) }
        dbbClient.putItem { r -> r.tableName(TABLE_NAME).item(product3.inDynamoDB()) }

        val result = adapter.findAllByCategory("Fruit")
        assertThat(result).containsExactlyInAnyOrder(product, product2)
    }

    private val product = Product(id = "1233213", name = "Orange", brand = null, category = "Fruit", description = null)
    private val product2 = product.copy(id = "1231231", category = "Fruit", brand = "Milka")
    private val product3 = product.copy(id = "432234", category = "Vegetable")

    private fun Product.inDynamoDB() = if (brand != null)
        mapOf(
            "category" to AttributeValue.fromS(category),
            "brand" to AttributeValue.fromS(brand),
            "id" to AttributeValue.fromS(id),
            "product" to AttributeValue.fromS(objectMapper.writeValueAsString(this))
        ) else mapOf(
        "category" to AttributeValue.fromS(category),
        "id" to AttributeValue.fromS(id),
        "product" to AttributeValue.fromS(objectMapper.writeValueAsString(this))
    )
}