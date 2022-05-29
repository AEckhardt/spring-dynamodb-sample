package com.example.springdynamodbsample.ports.validation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import javax.validation.Validator

@SpringBootTest(classes = [ValidationAutoConfiguration::class])
internal class NullOrNotBlankValidatorTest {

    @Autowired
    private lateinit var validator: Validator

    @Test
    fun `should be valid if string is null`() {
        val test = SimpleClass(null)
        val result = validator.validate(test)
        assertThat(result).isEmpty()
    }

    @Test
    fun `should be valid if string is not empty`() {
        val test = SimpleClass("1")
        val result = validator.validate(test)
        assertThat(result).isEmpty()
    }

    @Test
    fun `should be invalid if string is empty`() {
        val test = SimpleClass("")
        val result = validator.validate(test)
        assertThat(result).isNotEmpty
    }

    data class SimpleClass(
        @field:NullOrNotBlank
        val value: String?
    )
}