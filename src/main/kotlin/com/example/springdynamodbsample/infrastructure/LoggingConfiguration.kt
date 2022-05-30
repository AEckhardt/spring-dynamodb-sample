package com.example.springdynamodbsample.infrastructure

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class LoggingConfiguration {
    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        val loggingFilter = CommonsRequestLoggingFilter().apply {
            setIncludeClientInfo(false)
            setIncludeQueryString(true)
            setIncludePayload(true)
            setMaxPayloadLength(100)
            setIncludeHeaders(false)
        }
        return loggingFilter
    }
}
