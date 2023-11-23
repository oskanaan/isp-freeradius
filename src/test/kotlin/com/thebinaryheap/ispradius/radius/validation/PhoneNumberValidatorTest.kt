package com.thebinaryheap.ispradius.radius.validation

import com.thebinaryheap.ispradius.settings.Settings
import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PhoneNumberValidatorTest {

  @Mock
  private lateinit var mockContext: ConstraintValidatorContext

  @InjectMocks
  private lateinit var phoneNumberValidator: PhoneNumberValidator

  @Test
  fun `valid phone number should return true`() {
    val regex = Settings.phoneRegex
    Settings.phoneRegex = """\d{10}"""

    try {
      val validNumbers = listOf("1234567890", "9876543210")
      validNumbers.forEach { number ->
        val result = phoneNumberValidator.isValid(number, mockContext)
        assertThat(result).isTrue()
      }
    } finally {
      Settings.phoneRegex = regex
    }
  }

  @Test
  fun `invalid phone number should return false`() {
    val regex = Settings.phoneRegex
    Settings.phoneRegex = """\d{10}"""

    try{
      val invalidNumbers = listOf("123", "abc", "12345678901")
      invalidNumbers.forEach { number ->
        val result = phoneNumberValidator.isValid(number, mockContext)
        assertThat(result).isFalse()
      }

    } finally {
      Settings.phoneRegex = regex
    }

  }

  @Test
  fun `null phone number should return true since there is nothing to validate`() {
    val regex = Settings.phoneRegex
    Settings.phoneRegex = """\d{10}"""
    try{
      val result = phoneNumberValidator.isValid(null, mockContext)
      assertThat(result).isTrue()

    } finally {
      Settings.phoneRegex = regex
    }
  }

  @Test
  fun `phone number with spaces should return false`() {
    val regex = Settings.phoneRegex
    Settings.phoneRegex = """\d{10}"""

    try{
      val numbersWithSpaces = listOf(" 1234567890", "9876543210 ", "  1234567890  ")
      numbersWithSpaces.forEach { number ->
        val result = phoneNumberValidator.isValid(number, mockContext)
        assertThat(result).isFalse()
      }

    } finally {
      Settings.phoneRegex = regex
    }
  }

  @Test
  fun `valid phone number with dashes should return true`() {
    val regex = Settings.phoneRegex
    Settings.phoneRegex = """\d{3}-\d{3}-\d{4}"""

    try{
      val validNumbers = listOf("123-456-7890", "987-654-3210")
      validNumbers.forEach { number ->
        val result = phoneNumberValidator.isValid(number, mockContext)
        assertThat(result).isTrue()
      }

    } finally {
      Settings.phoneRegex = regex
    }
  }

  @Test
  fun `invalid phone number with dashes should return false`() {
    val regex = Settings.phoneRegex
    Settings.phoneRegex = """\d{3}-\d{3}-\d{4}"""

    try{
      val invalidNumbers = listOf("123-45", "abc-def-ghij", "123-456-78901")
      invalidNumbers.forEach { number ->
        val result = phoneNumberValidator.isValid(number, mockContext)
        assertThat(result).isFalse()
      }

    } finally {
      Settings.phoneRegex = regex
    }
  }

}
