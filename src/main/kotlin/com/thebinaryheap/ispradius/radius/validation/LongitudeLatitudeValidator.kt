package com.thebinaryheap.ispradius.radius.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.regex.Pattern
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Annotation to enforce the correct format of longitude and latitude inputs.
 * It uses `LongitudeLatitudeValidator` to validate that the provided value matches the required pattern.
 *
 * @property message The default error message to be used if validation fails.
 * @property groups Optional constraint groups the annotation belongs to.
 * @property payload Optional payload with which to associate the annotation.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [LongitudeLatitudeValidator::class])
annotation class ValidLongitudeLatitude(
  val message: String = "Invalid longitude or latitude format.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

/**
 * Validates whether a given string is a valid representation of longitude or latitude.
 * It uses a regular expression to check the format of the input string.
 */
class LongitudeLatitudeValidator : ConstraintValidator<ValidLongitudeLatitude, String> {
  private var log: Logger = LoggerFactory.getLogger(LongitudeLatitudeValidator::class.java)

  private val regex = "^(-?\\d+(\\.\\d+)?)\$"

  /**
   * Validates the input string to ensure it matches the pattern of a valid longitude or latitude.
   *
   * @param longLat The input string to validate.
   * @param p1 The context in which the constraint is evaluated.
   * @return True if the input is a valid longitude or latitude, false otherwise.
   */
  override fun isValid(longLat: String?, p1: ConstraintValidatorContext?): Boolean {
    log.debug("Validating longitude or latitude $longLat against expression $regex.")

    if (longLat.isNullOrBlank() || Pattern.compile(regex).matcher(longLat).matches()) {
      return true
    }
    log.debug("Longitude or latitude $longLat is invalid, correct format is $regex.")
    return false

  }
}