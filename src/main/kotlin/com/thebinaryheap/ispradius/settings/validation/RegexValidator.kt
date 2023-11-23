package com.thebinaryheap.ispradius.settings.validation

import com.thebinaryheap.ispradius.common.command.Command
import com.thebinaryheap.ispradius.common.command.CommandResponse
import com.thebinaryheap.ispradius.common.command.CommandStatus
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.regex.Pattern
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RegexValidator::class])
annotation class ValidRegex(
  val message: String = "Invalid regular expression.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

/**
 * Validates that a regular expression is valid.
 */
class RegexValidator : ConstraintValidator<ValidRegex, String> {
  private var log: Logger = LoggerFactory.getLogger(RegexValidator::class.java)

  override fun isValid(regex: String?, context: ConstraintValidatorContext): Boolean {
    log.debug("Validating that regular expression $regex is a valid regular expression.")

    if (regex != null && ValidateRegexPattern(regex).execute().status.isError()) {
      log.debug("Regular expression $regex is invalid.")
      return false
    }

    return true
  }
}

/**
 * Validates that a regular expression is correct and can be compiled.
 */
class ValidateRegexPattern(private val regex: String) : Command<ValidateRegexPattern> {

  private var log: Logger = LoggerFactory.getLogger(ValidateCronPattern::class.java)

  override fun execute(): CommandResponse<ValidateRegexPattern> {
    val commandResponse = CommandResponse<ValidateRegexPattern>(CommandStatus.SUCCESS)

    try {
      log.debug("Validating received regular expression $regex")
      Pattern.compile(regex)
    } catch (ex: Exception) {
      log.error("Invalid regex received $regex.", ex)
      commandResponse.status = CommandStatus.ERROR
      commandResponse.errorMessage = if (ex.message != null) ex.message!! else ""
    }

    return commandResponse
  }
}