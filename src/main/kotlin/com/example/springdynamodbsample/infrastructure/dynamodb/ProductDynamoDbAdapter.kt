package com.example.springdynamodbsample.infrastructure.dynamodb

import com.example.springdynamodbsample.domain.Product
import com.example.springdynamodbsample.domain.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.waiters.WaiterResponse
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse
import software.amazon.awssdk.services.dynamodb.model.ProjectionType.ALL
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException
import javax.annotation.PostConstruct

@Component
class ProductDynamoDbAdapter(
    private val dynamoDbClient: DynamoDbClient
) : ProductRepository {

    companion object {
        const val TABLE_NAME = "product"
        const val CATEGORY_INDEX = "category-product"
        const val BRAND_INDEX = "brand-product"
    }

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build()
    private val table: DynamoDbTable<ProductEntity> =
        dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromClass(ProductEntity::class.java))

    @PostConstruct
    fun createTableIfNotExists() {
        val provisionedThroughput = ProvisionedThroughput.builder()
            .readCapacityUnits(1L).writeCapacityUnits(1L).build()

        try {
            table.createTable { r ->
                r.provisionedThroughput(provisionedThroughput)
                    .globalSecondaryIndices(
                        EnhancedGlobalSecondaryIndex.builder().indexName(CATEGORY_INDEX)
                            .provisionedThroughput(provisionedThroughput)
                            .projection { p -> p.projectionType(ALL) }.build(),
                        EnhancedGlobalSecondaryIndex.builder().indexName(BRAND_INDEX)
                            .provisionedThroughput(provisionedThroughput)
                            .projection { p -> p.projectionType(ALL) }
                            .build()
                    )
            }
            val waiterResponse: WaiterResponse<DescribeTableResponse> =
                dynamoDbClient.waiter().waitUntilTableExists { r -> r.tableName(TABLE_NAME) }
            waiterResponse.matched().response().ifPresent { tableDescription ->
                log.info(
                    "Created table <tableName={}, indices={}>",
                    tableDescription.table().tableName(),
                    tableDescription.table().globalSecondaryIndexes().map { it.indexName() }
                )
            }
        } catch (e: ResourceInUseException) {
            log.info("Table exists already <tableName={}, indices={}>", TABLE_NAME, listOf(CATEGORY_INDEX, BRAND_INDEX))
        }
    }

    override fun save(product: Product) {
        val item = ProductEntity(
            id = product.id,
            brand = product.brand,
            category = product.category,
            product = product
        )
        table.putItem(item)
        log.info("Saved product <{}>", product)
    }

    override fun find(id: String): Product? {
        val key = Key.builder().partitionValue(id).build()
        return table.getItem(key)?.toProduct()
    }


    override fun delete(id: String): Product? {
        val key = Key.builder().partitionValue(id).build()
        return table.deleteItem(key)?.toProduct()?.also {
            log.info("Deleted product <{}>", it)
        }
    }

    override fun findAll(): List<Product> {
        return table.scan().items().asSequence().mapNotNull { it.product }.toList()
    }

    override fun findAllByCategory(category: String): List<Product> = queryIndex(CATEGORY_INDEX, category)

    override fun findAllByBrand(brand: String): List<Product> = queryIndex(BRAND_INDEX, brand)

    private fun queryIndex(indexName: String, partitionKey: String): List<Product> {
        val queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(partitionKey).build())
        val pageIterable = PageIterable.create(table.index(indexName).query(queryConditional))
        return pageIterable.items().asSequence().mapNotNull { it.product }.toList()
    }

    private fun ProductEntity.toProduct() = product
}
