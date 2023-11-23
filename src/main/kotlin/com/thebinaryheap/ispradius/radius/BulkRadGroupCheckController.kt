package com.thebinaryheap.ispradius.radius

import com.thebinaryheap.ispradius.radius.repo.RadGroupCheckRepository
import com.thebinaryheap.ispradius.radius.repo.RadGroupCheckEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * REST controller for performing bulk operations on RadGroupCheck entities.
 *
 * @property radGroupCheckRepository The repository to perform operations on RadGroupCheck entities.
 */
@RestController
@RequestMapping("/radgroupcheck/bulk")
class BulkRadGroupCheckController(private val radGroupCheckRepository: RadGroupCheckRepository) {

  /**
   * Deletes a list of RadGroupCheck entities in bulk.
   *
   * @param radGroupCheckEntities A list of RadGroupCheck entities to be deleted.
   */
  @DeleteMapping("/delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun bulkDelete(@RequestBody radGroupCheckEntities: List<RadGroupCheckEntity>) {
    radGroupCheckRepository.deleteAll(radGroupCheckEntities)
  }
}
