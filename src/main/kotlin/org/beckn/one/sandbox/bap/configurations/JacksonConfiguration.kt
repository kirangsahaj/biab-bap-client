package org.beckn.one.sandbox.bap.configurations

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.util.StdDateFormat
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class JacksonConfiguration {
  @Bean
  fun objectMapper(): ObjectMapper {
    val objectMapper = ObjectMapper()
    objectMapper.findAndRegisterModules()
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    objectMapper.dateFormat = StdDateFormat().withColonInTimeZone(true)
    objectMapper.propertyNamingStrategy = PropertyNamingStrategies.SnakeCaseStrategy()
    return objectMapper
  }
}