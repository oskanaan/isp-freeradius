package com.thebinaryheap.ispradius.radius

import com.thebinaryheap.ispradius.radius.repo.RadGroupReplyEntity
import com.thebinaryheap.ispradius.radius.repo.RadGroupReplyRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * REST controller for performing bulk operations on RadGroupReply entities.
 *
 * @property radGroupReplyRepository The repository to perform operations on RadGroupReply entities.
 */
@RestController
@RequestMapping("/radgroupreply/bulk")
class BulkRadGroupReplyController(private val radGroupReplyRepository: RadGroupReplyRepository) {

  /**
   * Deletes a list of RadGroupReply entities in bulk.
   *
   * @param radGroupReplies A list of RadGroupReply entities to be deleted.
   */
  @DeleteMapping("/delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun bulkDelete(@RequestBody radGroupReplies: List<RadGroupReplyEntity>) {
    radGroupReplyRepository.deleteAll(radGroupReplies)
  }
}
