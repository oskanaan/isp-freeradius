package com.thebinaryheap.ispradius.radius.repo

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

/**
 * Repository interface for managing RadUserGroup entities. It extends PagingAndSortingRepository and CrudRepository for providing pagination and CRUD operations.
 */
@RepositoryRestResource(collectionResourceRel = "radusergroups", path = "radusergroups")
interface RadUserGroupRepository : PagingAndSortingRepository<RadUserGroupEntity, Long>, CrudRepository<RadUserGroupEntity, Long> {
  fun findByGroupNameIgnoreCase(groupName: String, pageable: Pageable): Page<RadUserGroupEntity>
  fun findByUsernameIgnoreCase(username: String, pageable: Pageable): Page<RadUserGroupEntity>
  fun deleteByUsername(username: String)
  fun findByUsername(username: String): List<RadUserGroupEntity>
}


/**
 * Entity representing a radusergroup, which maps usernames to groups with a priority for RADIUS processing.
 *
 * @property id The unique identifier of the radusergroup entry, generated automatically.
 * @property username The username associated with this entry; it must not be empty as per validation constraints.
 * @property groupName The group name associated with this entry; it must not be empty as per validation constraints.
 * @property priority An integer representing the priority of this group for the user; defaults to 0.
 */
@Entity
@Table(name = "radusergroup")
data class RadUserGroupEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @Column(name = "username")
  @field:NotEmpty(message = "{empty.username}")
  var username: String? = null,

  @Column(name = "groupname")
  @field:NotEmpty(message = "{empty.groupName}")
  var groupName: String? = null,

  @Column(name = "priority")
  var priority: Int = 0
)
