package com.thebinaryheap.ispradius.settings.validation

import com.thebinaryheap.ispradius.common.command.CommandStatus
import jakarta.validation.ConstraintValidatorContext
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

class RegexValidatorTest {

  @InjectMocks
  lateinit var regexValidator: RegexValidator

  @BeforeEach
  fun setup() {
    MockitoAnnotations.openMocks(this)
  }

  @Test
  fun `test isValid should return true for valid regex`() {
    val validRegex = "[a-zA-Z0-9]*"
    assertTrue(regexValidator.isValid(validRegex, mock(ConstraintValidatorContext::class.java)))
  }

  @Test
  fun `test isValid should return false for invalid regex`() {
    val invalidRegex = "[a-z"
    assertTrue(!regexValidator.isValid(invalidRegex, mock(ConstraintValidatorContext::class.java)))
  }
}

class ValidateRegexPatternTest {

  @Test
  fun `test execute should return success for valid regex`() {
    val validRegex = "[a-zA-Z0-9]*"
    val command = ValidateRegexPattern(validRegex)
    val response = command.execute()
    assertEquals(CommandStatus.SUCCESS, response.status)
  }

  @Test
  fun `test execute should return error for invalid regex`() {
    val invalidRegex = "[a-z"
    val command = ValidateRegexPattern(invalidRegex)
    val response = command.execute()
    assertEquals(CommandStatus.ERROR, response.status)
  }
}
