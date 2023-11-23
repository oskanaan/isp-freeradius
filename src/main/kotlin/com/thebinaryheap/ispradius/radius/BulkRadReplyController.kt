package com.thebinaryheap.ispradius.radius

import com.thebinaryheap.ispradius.radius.repo.RadReplyEntity
import com.thebinaryheap.ispradius.radius.repo.RadReplyRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * REST controller for performing bulk operations on RadReply entities.
 *
 * @property radReplyRepository The repository to perform CRUD operations on RadReply entities.
 */
@RestController
@RequestMapping("/radreply/bulk")
class BulkRadReplyController(private val radReplyRepository: RadReplyRepository) {

  /**
   * Deletes a list of RadReply entities in bulk.
   *
   * @param radReplies A list of RadReply entities to be deleted.
   */
  @DeleteMapping("/delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun bulkDelete(@RequestBody radReplies: List<RadReplyEntity>) {
    radReplyRepository.deleteAll(radReplies)
  }
}
