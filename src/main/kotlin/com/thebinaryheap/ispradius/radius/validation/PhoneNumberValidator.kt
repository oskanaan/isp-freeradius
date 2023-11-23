package com.thebinaryheap.ispradius.radius.validation

import com.thebinaryheap.ispradius.settings.Settings
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.regex.Pattern
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Annotation to enforce the correct format of phone number inputs.
 * It uses `PhoneNumberValidator` to validate that the provided value matches the format defined in [Settings].
 *
 * @property message The default error message to be used if validation fails.
 * @property groups Optional constraint groups the annotation belongs to.
 * @property payload Optional payload with which to associate the annotation.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PhoneNumberValidator::class])
annotation class ValidPhoneNumber(
  val message: String = "Invalid phone number format",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

/**
 * Validates whether a given string is a valid representation of a phone number.
 * The phone number format is defined in [Settings.phoneRegex].
 */
class PhoneNumberValidator : ConstraintValidator<ValidPhoneNumber, String> {
  private var log: Logger = LoggerFactory.getLogger(PhoneNumberValidator::class.java)

  /**
   * Validates the input string to ensure it matches the phone number pattern defined in [Settings.phoneRegex].
   *
   * @param phoneNumber The input string to validate.
   * @param context The context in which the constraint is evaluated.
   * @return True if the input is a valid phone number, false otherwise.
   */
  override fun isValid(phoneNumber: String?, context: ConstraintValidatorContext): Boolean {
    log.debug("Validating phone number $phoneNumber against expression ${Settings.phoneRegex}.")

    if (phoneNumber != null && !Pattern.compile(Settings.phoneRegex).matcher(phoneNumber).matches()) {
      log.debug("Phone number $phoneNumber is invalid, correct format is ${Settings.phoneRegex}.")
      return false
    }

    return true
  }
}