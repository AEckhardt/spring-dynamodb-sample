package com.example.springdynamodbsample

import com.example.springdynamodbsample.infrastructure.dynamodb.ProductDynamoDbAdapter
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(
    properties = ["aws.region=eu-central-1", "aws.dynamodb.endpoint=localhost:1234"]
)
@MockBean(ProductDynamoDbAdapter::class)
class SpringDynamodbSampleApplicationTests {

    @Test
    fun contextLoads() {
    }

}
