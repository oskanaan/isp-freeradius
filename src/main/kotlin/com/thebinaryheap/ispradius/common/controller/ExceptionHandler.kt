package com.thebinaryheap.ispradius.common.controller

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.thebinaryheap.ispradius.common.errors.ManagedAttributeException
import com.thebinaryheap.ispradius.common.errors.UniqueValueException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.hibernate.validator.internal.engine.ConstraintViolationImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.lang.Nullable
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Centralized exception handling across all `@RequestMapping` methods through `@ExceptionHandler` methods.
 */
@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

  private var log: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

  /**
   * Handles `ConstraintViolationException` when a validation constraint is violated.
   *
   * @param ex The exception that was thrown.
   * @param request The web request during which the exception was thrown.
   * @return A `ResponseEntity` containing the `ApiError` with BAD_REQUEST status.
   */
  @org.springframework.web.bind.annotation.ExceptionHandler(value = [ConstraintViolationException::class])
  fun handleConstraintViolationException(ex: ConstraintViolationException, request: WebRequest): ResponseEntity<Any>? {
    log.error("Handling ConstraintViolationException exception.", ex)

    val constraintViolations: MutableSet<ConstraintViolation<*>>? = ex.constraintViolations

    val errors = constraintViolations?.map {
      ApiError.Error(it.propertyPath.last().name, it.message)
    } ?: listOf()

    return ResponseEntity.badRequest().body(ApiError(HttpStatus.BAD_REQUEST, ex.message!!, errors))
  }

  /**
   * Handles exceptions when method arguments are not valid. Overrides the `handleMethodArgumentNotValid`
   * method from `ResponseEntityExceptionHandler`.
   *
   * @param ex The exception that was thrown.
   * @param headers The headers to be written to the response.
   * @param status The selected response status.
   * @param request The web request during which the exception was thrown.
   * @return A `ResponseEntity` containing the `ApiError` with BAD_REQUEST status.
   */
  @Nullable
  override fun handleMethodArgumentNotValid(
    ex: MethodArgumentNotValidException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest
  ): ResponseEntity<Any>? {
    val errors = ex.bindingResult.fieldErrors.map { error ->
      ApiError.Error(error.field, error.defaultMessage.toString())
    }
    return ResponseEntity.badRequest().body(ApiError(HttpStatus.BAD_REQUEST, ex.message, errors))
  }

  /**
   * Handles `RepositoryConstraintViolationException` which is thrown when a repository method constraint is violated.
   *
   * @param ex The exception object containing repository constraint violations.
   * @param request The web request during which the exception was raised.
   * @return A `ResponseEntity` containing the details of the API error.
   */
  @org.springframework.web.bind.annotation.ExceptionHandler(value = [RepositoryConstraintViolationException::class])
  fun handleRepositoryConstraintViolationException(ex: RepositoryConstraintViolationException, request: WebRequest): ResponseEntity<Any>? {
    log.error("Handling RepositoryConstraintViolationException exception.", ex)

    val errors = ex.errors.allErrors.map { error ->
      val property = error.unwrap(ConstraintViolationImpl::class.java).propertyPath.last().name
      ApiError.Error(property, error.defaultMessage ?: "")
    }

    return ResponseEntity.badRequest().body(ApiError(HttpStatus.BAD_REQUEST, ex.message!!, errors))
  }

  /**
   * Handles `UniqueValueException` which is thrown when an attempt to insert or update a resource
   * with a value that must be unique violates this constraint.
   *
   * @param ex The `UniqueValueException` that was thrown when a unique constraint was violated.
   * @param request The web request during which the exception occurred.
   * @return A `ResponseEntity` containing the `ApiError` with details about the violated constraint,
   *         including the field name and error message, along with a BAD_REQUEST status.
   */
  @org.springframework.web.bind.annotation.ExceptionHandler(value = [UniqueValueException::class])
  fun handleUniqueValueException(ex: UniqueValueException, request: WebRequest): ResponseEntity<Any>? {
    log.error("Handling UniqueValueException exception.", ex)

    val errors = listOf(ApiError.Error(ex.fieldName, ex.message!!))

    return ResponseEntity.badRequest().body(ApiError(HttpStatus.BAD_REQUEST, ex.message!!, errors))
  }

  /**
   * Handles `ManagedAttributeException` which is thrown when an operation on a managed attribute fails.
   * This can occur when there is an attempt to modify an attribute that should not be changed or when
   * there is a violation of attribute constraints.
   *
   * @param ex The `ManagedAttributeException` instance containing details about the specific attribute error.
   * @param request The web request during which the exception occurred.
   * @return A `ResponseEntity` containing the `ApiError` object which includes details of the exception,
   *         such as the field name and the error message, along with the HTTP status code for a bad request.
   */
  @org.springframework.web.bind.annotation.ExceptionHandler(value = [ManagedAttributeException::class])
  fun handleManagedAttributeException(ex: ManagedAttributeException, request: WebRequest): ResponseEntity<Any>? {
    log.error("Handling ManagedAttributeException exception.", ex)

    val errors = listOf(ApiError.Error(ex.fieldName, ex.message!!))

    return ResponseEntity.badRequest().body(ApiError(HttpStatus.BAD_REQUEST, ex.message!!, errors))
  }

  /**
   * Handles `DataIntegrityViolationException` which typically occurs when a database integrity constraint is violated.
   *
   * @param ex The exception object containing details about the data integrity violation.
   * @param request The web request during which the exception was raised.
   * @return A `ResponseEntity` containing the details of the API error.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @org.springframework.web.bind.annotation.ExceptionHandler(value = [DataIntegrityViolationException::class])
  fun handleDataIntegrityViolationException(ex: DataIntegrityViolationException, request: WebRequest): ResponseEntity<Any>? {
    log.error("Handling DataIntegrityViolationException exception.", ex)
    val apiError = ApiError(HttpStatus.BAD_REQUEST, messageSource?.getMessage("error.entity.in.use", null, LocaleContextHolder.getLocale())!!, listOf())
    apiError.errorCode = "DATA_INTEGRITY"
    return ResponseEntity.badRequest().body(apiError)
  }

  /**
   * Handles `MethodArgumentTypeMismatchException` which is typically thrown when a method parameter
   * is not the expected type due to a type mismatch in the client request. For instance, this can occur
   * when a client provides a string that cannot be converted to a number for a numeric parameter.
   *
   * The method is annotated with `@ResponseStatus` to indicate that it always responds with
   * a BAD_REQUEST (400) HTTP status code when this exception occurs.
   *
   * @param ex The `MethodArgumentTypeMismatchException` containing details about the type mismatch error.
   * @param request The web request during which the exception occurred.
   * @return A `ResponseEntity` containing an `ApiError` with a BAD_REQUEST status and details about the
   *         type mismatch, including the name of the parameter and a message indicating that the provided
   *         value is invalid.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @org.springframework.web.bind.annotation.ExceptionHandler(value = [MethodArgumentTypeMismatchException::class])
  fun handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException, request: WebRequest): ResponseEntity<Any>? {
    log.error("Handling MethodArgumentTypeMismatchException exception.", ex)
    val apiError = ApiError(HttpStatus.BAD_REQUEST,
      messageSource?.getMessage("error.invalid.argument.value", null, LocaleContextHolder.getLocale())!!,
      listOf(ApiError.Error(ex.name, messageSource?.getMessage("error.invalid.value.provided", null, LocaleContextHolder.getLocale())!!)))
    return ResponseEntity.badRequest().body(apiError)
  }


  override fun handleHttpMessageNotReadable(
    ex: HttpMessageNotReadableException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest
  ): ResponseEntity<Any>? {
    log.error("Handling MethodArgumentTypeMismatchException exception.", ex)
    if (ex.cause != null && ex.cause is InvalidFormatException) {
      val cause = (ex.cause!! as InvalidFormatException)
      val messageText = if (cause.targetType == Integer::class.java) {
        messageSource?.getMessage("error.invalid.argument.number.format", null, LocaleContextHolder.getLocale())
      } else {
        messageSource?.getMessage("error.invalid.argument.format", null, LocaleContextHolder.getLocale())
      }

      val errors = cause.path?.map { ApiError.Error(it.fieldName, messageText!!) }
      val apiError = ApiError(HttpStatus.BAD_REQUEST,
        messageText!!,
        errors!!)
      return ResponseEntity.badRequest().body(apiError)
    }

    return super.handleHttpMessageNotReadable(ex, headers, status, request)
  }
}