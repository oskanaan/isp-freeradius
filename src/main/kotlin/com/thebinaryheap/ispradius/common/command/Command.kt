package com.thebinaryheap.ispradius.common.command

/**
 * A functional interface representing a command that can be executed.
 * This command returns a CommandResponse with a type parameter indicating the expected result type.
 *
 * @param T The type of the result that the command produces.
 */
fun interface Command<T> {
  /**
   * Executes the command.
   *
   * @return A CommandResponse of type T, containing the outcome of the execution.
   */
  fun execute() : CommandResponse<T>
}

/**
 * A data class representing the response returned by executing a command.
 * It includes the command's execution status, an optional error message, and the result data if available.
 *
 * @param T The type of the data that the command produces.
 * @property status The status of the command after execution, indicating success or error.
 * @property errorMessage An optional message providing details in case of an error. It defaults to an empty string.
 * @property data The data resulting from the command execution, which may be null.
 */
data class CommandResponse<T> (
  var status: CommandStatus,
  var errorMessage: String = "",
  var data: T? = null
) {
  /**
   * Checks if the command resulted in an error.
   *
   * @return True if the status indicates an error, false otherwise.
   */
  fun hasErrors(): Boolean {
    return status.isError()
  }

}

/**
 * Enum representing the possible statuses of a command's execution.
 *
 * @property value The string representation of the status.
 */
enum class CommandStatus (private val value: String) {
  SUCCESS("1"), ERROR("2");

  fun isSuccess() : Boolean {
    return this.value == SUCCESS.value
  }

  fun isError() : Boolean {
    return this.value == ERROR.value
  }
}