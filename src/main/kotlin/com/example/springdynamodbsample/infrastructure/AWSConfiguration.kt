package com.example.springdynamodbsample.infrastructure

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

@Configuration
class AWSConfiguration(
    @Value("\${aws.region}") private val region: String,
    @Value("\${aws.dynamodb.endpoint}") private val endpoint: String
) {

    @Bean
    fun awsCredentialsProvider(): AwsCredentialsProvider {
        return DefaultCredentialsProvider.create()
    }

    @Bean
    fun dynamoDBClient(credentialsProvider: AwsCredentialsProvider): DynamoDbClient {
        return DynamoDbClient.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(credentialsProvider)
            .build()
    }
}