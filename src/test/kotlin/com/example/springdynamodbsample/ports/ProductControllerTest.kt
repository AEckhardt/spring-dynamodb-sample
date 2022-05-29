package com.example.springdynamodbsample.ports

import com.example.springdynamodbsample.domain.Product
import com.example.springdynamodbsample.domain.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @MockBean
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should get all products`() {
        mockMvc.perform(get("/products").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""[]"""))
    }

    @Test
    fun `should get product`() {
        doReturn(product).whenever(productRepository).find(id)
        mockMvc.perform(get("/products/$id").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(productAsJson()))
    }

    @Test
    fun `should throw NOT_FOUND if product is missing`() {
        mockMvc.perform(get("/products/$id").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should delete product`() {
        doReturn(product).whenever(productRepository).delete(id)

        mockMvc.perform(delete("/products/$id").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun `should throw NOT_FOUND if product did not exist`() {
        mockMvc.perform(get("/products/$id").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should post new product`() {
        mockMvc.perform(
            post("/products").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(productAsJson()))

        argumentCaptor<Product>() {
            verify(productRepository).save(capture())
            assertThat(firstValue).usingRecursiveComparison().ignoringFields("lastUpdated").isEqualTo(product)
        }
    }

    @Test
    fun `should throw BAD_REQUEST if product is invalid`() {
        val invalidProductDto = ProductDto("", "", "", category = "", description = null)
        mockMvc.perform(
            post("/products").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should get all products by brand`() {
        doReturn(listOf(product)).whenever(productRepository).findAllByBrand("Milka")
        mockMvc.perform(get("/products/brand/Milka").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""[${productAsJson()}]"""))
    }

    @Test
    fun `should get all products by category`() {
        doReturn(listOf(product)).whenever(productRepository).findAllByCategory("Schokolade")
        mockMvc.perform(get("/products/category/Schokolade").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""[${productAsJson()}]"""))
    }

    private val id = "12331"
    private val productDto = ProductDto(
        id = id,
        name = "Milka Vollmilch Schokolade",
        description = "Ich bin eine Schokolade",
        brand = "Milka",
        category = "Schokolade"
    )
    private val product =
        Product(
            id = id,
            name = "Milka Vollmilch Schokolade",
            brand = "Milka",
            category = "Schokolade",
            description = "Ich bin eine Schokolade"
        )

    private fun productAsJson() = objectMapper.writeValueAsString(productDto)
}