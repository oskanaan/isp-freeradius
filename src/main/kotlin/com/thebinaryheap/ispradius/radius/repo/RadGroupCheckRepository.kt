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
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

/**
 * Repository interface for managing RadGroupCheck entities, providing custom queries for searching based on group name and assignment status.
 */
@RepositoryRestResource(collectionResourceRel = "radgroupcheck", path = "radgroupcheck")
interface RadGroupCheckRepository : PagingAndSortingRepository<RadGroupCheckEntity, Long>, CrudRepository<RadGroupCheckEntity, Long> {
  @Query(
    "SELECT n FROM RadGroupCheckEntity n WHERE " +
        "(:groupName IS NULL OR LOWER(n.groupName) LIKE LOWER(CONCAT('%', :groupName, '%'))) "
  )
  fun findByGroupNameIgnoreCase(@Param("groupName") groupName: String?, pageable: Pageable): Page<RadGroupCheckEntity>

  @Query("SELECT r FROM RadGroupCheckEntity r WHERE r.groupName NOT IN " +
      "(SELECT u.groupName FROM RadUserGroupEntity u WHERE u.username = :username) AND LOWER(r.groupName) like LOWER(CONCAT('%', :groupName, '%'))")
  fun findGroupsNotAssignedToUser(@Param("username") username: String?, @Param("groupName") groupName: String, pageable: Pageable): Page<RadGroupCheckEntity>

  fun findByGroupName(groupName: String?): List<RadGroupCheckEntity>

}

/**
 * Entity representing a radgroupcheck, which defines attributes for group-based RADIUS checks.
 *
 * @property id The unique identifier of the radgroupcheck entry.
 * @property groupName The name of the group this entry is associated with, must not be empty.
 * @property attribute The RADIUS attribute for this entry, must not be empty.
 * @property operator The operator to apply in the RADIUS check for this entry, must not be empty.
 * @property value The value to be used in the check for this entry, must not be empty.
 */
@Entity
@Table(name = "radgroupcheck")
data class RadGroupCheckEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @field:NotEmpty(message = "{empty.groupName}")
  @Column(name = "groupname")
  var groupName: String? = null,

  @field:NotEmpty(message = "{empty.attribute}")
  @Column(name = "attribute")
  var attribute: String? = null,

  @field:NotEmpty(message = "{empty.operator}")
  @Column(name = "op")
  var operator: String? = null,

  @field:NotEmpty(message = "{empty.value}")
  @Column(name = "\"value\"")
  var value: String? = null
)