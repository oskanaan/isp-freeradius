package com.thebinaryheap.ispradius.common.controller

import org.springframework.http.HttpStatus

/**
 * Represents the structure of an API error response.
 * It includes HTTP status, error code, a message, and a list of detailed errors.
 */
class ApiError(status: HttpStatus, message: String, errors: List<Error>) {
  var status: HttpStatus = status
    private set

  var errorCode: String? = ""

  var message: String = message
    private set

  var errors: List<Error> = errors
    private set

  /**
   * Data class representing a detailed error, typically related to a specific field in a request.
   *
   * @param field The name of the field that has the error.
   * @param message The error message associated with the field.
   */
  data class Error(val field: String, val message: String)
}