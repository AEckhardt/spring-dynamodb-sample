package com.example.springdynamodbsample.ports

import com.example.springdynamodbsample.ports.validation.NullOrNotBlank
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class ProductDto(
    @field:NotBlank
    val id: String,
    @field:NotBlank
    val name: String,
    @field:NullOrNotBlank
    val brand: String?,
    @field:NotBlank
    val category: String,
    @field:Size(max = 200)
    val description: String?
)

