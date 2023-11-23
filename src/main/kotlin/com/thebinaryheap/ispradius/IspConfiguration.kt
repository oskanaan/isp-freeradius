package com.thebinaryheap.ispradius

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
class IspConfiguration {
  @Bean
  fun validatorFactoryBean(): LocalValidatorFactoryBean? {
    val bean = LocalValidatorFactoryBean()
    bean.setValidationMessageSource(messageSource());
    return bean
  }

  @Bean
  fun messageSource(): MessageSource {
    val messageSource = ResourceBundleMessageSource()
    messageSource.setBasename("messages")
    messageSource.setDefaultEncoding("UTF-8")
    return messageSource
  }
}

@Configuration
class CorsConfig {

  @Value("\${app.corsOrigin}")
  lateinit var allowedOrigin: String

  @Bean
  fun corsFilter(): CorsFilter {
    val source = UrlBasedCorsConfigurationSource()
    val config = CorsConfiguration()

    // Set allowed origins, methods, and headers
    config.addAllowedOrigin(allowedOrigin)
    config.addAllowedMethod("*")
    config.addAllowedHeader("Content-Type");
    config.addAllowedHeader("Authorization");
    source.registerCorsConfiguration("/**", config)
    return CorsFilter(source)
  }
}


@ControllerAdvice
class ControllerConfig {
  @InitBinder
  fun initBinder(dataBinder: WebDataBinder) {
    // Use StringTrimmerEditor to convert empty strings to null
    dataBinder.registerCustomEditor(String::class.java, StringTrimmerEditor(true))
  }
}


@Configuration
class JacksonConfig {

  @Autowired
  fun customizeObjectMapper(objectMapper: ObjectMapper) {
    val javaTimeModule = JavaTimeModule()

    // Custom serializer
    javaTimeModule.addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
      @Throws(IOException::class)
      override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
      }
    })

    // Custom deserializer
    javaTimeModule.addDeserializer(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime>() {
      @Throws(IOException::class)
      override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(p.text, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      }
    })

    objectMapper.registerModule(javaTimeModule)
  }
}