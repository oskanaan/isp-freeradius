package com.thebinaryheap.ispradius.radius

import com.thebinaryheap.ispradius.radius.repo.RadUserGroupEntity
import com.thebinaryheap.ispradius.radius.repo.RadUserGroupRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * REST controller for performing bulk operations on RadUserGroup entities.
 *
 * @property radUserGroupRepository The repository to perform CRUD operations on RadUserGroup entities.
 */
@RestController
@RequestMapping("/radusergroups/bulk")
class BulkRadUserGroupController(private val radUserGroupRepository: RadUserGroupRepository) {

  /**
   * Creates RadUserGroup entities in bulk.
   *
   * @param userGroups A list of RadUserGroup entities to be created.
   * @return A mutable iterable of the created RadUserGroup entities.
   */
  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  fun createBulkRadUserGroups(@RequestBody userGroups: List<RadUserGroupEntity>): MutableIterable<RadUserGroupEntity> {
    return radUserGroupRepository.saveAll(userGroups)
  }

  /**
   * Deletes a list of RadUserGroup entities in bulk.
   *
   * @param userGroups A list of RadUserGroup entities to be deleted.
   */
  @DeleteMapping("/delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteBulkRadUserGroups(@RequestBody userGroups: List<RadUserGroupEntity>) {
    radUserGroupRepository.deleteAll(userGroups)
  }
}
