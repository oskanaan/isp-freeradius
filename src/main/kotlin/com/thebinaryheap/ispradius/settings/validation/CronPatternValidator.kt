package com.thebinaryheap.ispradius.settings.validation

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinition
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import com.thebinaryheap.ispradius.common.command.Command
import com.thebinaryheap.ispradius.common.command.CommandResponse
import com.thebinaryheap.ispradius.common.command.CommandStatus
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Annotation to validate that a string is a valid CRON pattern.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CronPatternValidator::class])
annotation class ValidCronPattern(
  val message: String = "Invalid CRON pattern.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

/**
 * Validates that a regular expression is valid.
 */
class CronPatternValidator : ConstraintValidator<ValidCronPattern, String> {
  private var log: Logger = LoggerFactory.getLogger(CronPatternValidator::class.java)

  override fun isValid(cronPattern: String?, context: ConstraintValidatorContext): Boolean {
    log.debug("Validating that CRON pattern $cronPattern is a valid regular expression.")

    if (cronPattern != null && ValidateCronPattern(cronPattern).execute().status.isError()) {
      log.debug("CRON pattern $cronPattern is invalid.")
      return false
    }

    return true
  }
}

/**
 * Validates that a CRON pattern is correct.
 */
class ValidateCronPattern(private val cronExpression: String) : Command<ValidateCronPattern> {

  private var log: Logger = LoggerFactory.getLogger(ValidateCronPattern::class.java)

  override fun execute(): CommandResponse<ValidateCronPattern> {
    val cronDefinition: CronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)

    val parser = CronParser(cronDefinition)

    val commandResponse = CommandResponse<ValidateCronPattern>(CommandStatus.SUCCESS)
    try {
      log.debug("Validating CRON pattern $cronExpression.")
      parser.parse(cronExpression)
    } catch (ex: Exception) {
      log.error("Received an invalid CRON pattern $cronExpression.", ex)
      commandResponse.status = CommandStatus.ERROR
      commandResponse.errorMessage = if (ex.message != null) ex.message!! else ""
    }

    return commandResponse
  }
}