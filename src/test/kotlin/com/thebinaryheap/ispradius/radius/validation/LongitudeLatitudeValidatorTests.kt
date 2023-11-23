package com.thebinaryheap.ispradius.radius.validation

import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class LongitudeLatitudeValidatorTest {

  @Mock
  private lateinit var mockConstraintValidatorContext: ConstraintValidatorContext

  @InjectMocks
  private lateinit var longitudeLatitudeValidator: LongitudeLatitudeValidator

  @Test
  fun `valid longitude or latitude should return true`() {
    val validValues = listOf("0", "0.0", "-90", "90", "-180", "180", "42.123456", "-56.789012")
    validValues.forEach { value ->
      val result = longitudeLatitudeValidator.isValid(value, mockConstraintValidatorContext)
      assertThat(result).isTrue()
    }
  }

  @Test
  fun `invalid longitude or latitude should return false`() {
    val invalidValues = listOf("invalid", "abc", "12.345.678", "42.12.34", "42.", ".123", "42..123")
    invalidValues.forEach { value ->
      val result = longitudeLatitudeValidator.isValid(value, mockConstraintValidatorContext)
      assertThat(result).isFalse()
    }
  }

  @Test
  fun `null longitude or latitude should return true`() {
    val result = longitudeLatitudeValidator.isValid(null, mockConstraintValidatorContext)
    assertThat(result).isTrue()
  }

  @Test
  fun `longitude or latitude with spaces should return false`() {
    val valuesWithSpaces = listOf(" 0", "0 ", " 0 ", " 42.123456 ", " -56.789012 ")
    valuesWithSpaces.forEach { value ->
      val result = longitudeLatitudeValidator.isValid(value, mockConstraintValidatorContext)
      assertThat(result).isFalse()
    }
  }

  @Test
  fun `longitude or latitude with leading - trailing whitespace should be trimmed`() {
    val result = longitudeLatitudeValidator.isValid("  42.123456  ", mockConstraintValidatorContext)
    assertThat(result).isFalse()
  }
}
