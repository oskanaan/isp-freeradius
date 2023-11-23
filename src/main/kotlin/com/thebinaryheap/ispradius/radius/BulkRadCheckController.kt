package com.thebinaryheap.ispradius.radius

import com.thebinaryheap.ispradius.radius.repo.RadcheckEntity
import com.thebinaryheap.ispradius.radius.repo.RadcheckRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * Rest controller for performing bulk operations on Radcheck entities.
 *
 * @property radcheckRepository The repository to perform operations on Radcheck entities.
 */
@RestController
@RequestMapping("/radcheck/bulk")
class BulkRadCheckController(private val radcheckRepository: RadcheckRepository) {

  /**
   * Deletes a list of Radcheck entities in bulk.
   *
   * @param radChecks A list of Radcheck entities to be deleted.
   */
  @DeleteMapping("/delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun bulkDelete(@RequestBody radChecks: List<RadcheckEntity>) {
    radcheckRepository.deleteAll(radChecks)
  }
}
