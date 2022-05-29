package com.example.springdynamodbsample.ports

import com.example.springdynamodbsample.domain.Product
import com.example.springdynamodbsample.domain.ProductRepository
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("/products")
class ProductController(
    private val productRepository: ProductRepository
) {

    @Operation(summary = "Get all products")
    @GetMapping(produces = ["application/json"])
    fun getAllProducts(): List<ProductDto> {
        return productRepository.findAll().map { it.toProductDto() }
    }

    @Operation(summary = "Get a product by id")
    @GetMapping("{id}", produces = ["application/json"])
    fun getProduct(@PathVariable id: String): ProductDto {
        return productRepository.find(id)?.toProductDto() ?: throw ResponseStatusException(NOT_FOUND)
    }

    @Operation(summary = "Create a product")
    @ResponseStatus(CREATED)
    @PostMapping(produces = ["application/json"], consumes = ["application/json"])
    fun addProduct(@Valid @RequestBody productDto: ProductDto): ProductDto {
        productRepository.save(productDto.toProduct())
        return productDto
    }

    @Operation(summary = "Delete a product by id")
    @DeleteMapping("{id}", produces = ["application/json"])
    fun deleteProduct(@PathVariable id: String): ProductDto {
        return productRepository.delete(id)?.toProductDto() ?: throw ResponseStatusException(NOT_FOUND)
    }

    @Operation(summary = "Get all products of a brand")
    @GetMapping("/brand/{brand}", produces = ["application/json"])
    fun getByBrand(@PathVariable brand: String): List<ProductDto> {
        return productRepository.findAllByBrand(brand).map { it.toProductDto() }
    }

    @Operation(summary = "Get all products of a category")
    @GetMapping("/category/{category}", produces = ["application/json"])
    fun getByCategory(@PathVariable category: String): List<ProductDto> {
        return productRepository.findAllByCategory(category).map { it.toProductDto() }
    }

    private fun Product.toProductDto() =
        ProductDto(id = id, name = name, brand = brand, category = category, description = description)

    private fun ProductDto.toProduct() =
        Product(id = id, name = name, brand = brand, category = category, description = description)
}
