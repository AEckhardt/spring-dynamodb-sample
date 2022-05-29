package com.example.springdynamodbsample.ports.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

@Target(FIELD, VALUE_PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = [NullOrNotBlankValidator::class])
@MustBeDocumented
annotation class NullOrNotBlank(
    val message: String = "Must be null or not blank!",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = []
)

class NullOrNotBlankValidator : ConstraintValidator<NullOrNotBlank, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.isNotBlank() ?: true
    }
}
