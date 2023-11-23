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
 * Repository interface for managing RadGroupReply entities. It extends PagingAndSortingRepository and CrudRepository.
 */
@RepositoryRestResource(collectionResourceRel = "radgroupreply", path = "radgroupreply")
interface RadGroupReplyRepository : PagingAndSortingRepository<RadGroupReplyEntity, Long>,CrudRepository<RadGroupReplyEntity, Long> {
  @Query(
    "SELECT n FROM RadGroupReplyEntity n WHERE " +
        "(:groupName IS NULL OR LOWER(n.groupName) LIKE LOWER(CONCAT('%', :groupName, '%'))) "
  )
  fun findByGroupNameIgnoreCase(@Param("groupName") groupName: String?, pageable: Pageable): Page<RadGroupReplyEntity>

  fun findByGroupName(groupName: String?): List<RadGroupReplyEntity>

}

/**
 * Entity representing a radgroupreply, which defines attributes for group-based RADIUS replies.
 *
 * @property id The unique identifier of the radgroupreply entry.
 * @property groupName The name of the group this entry is associated with; it must not be empty.
 * @property attribute The RADIUS attribute for this entry; it must not be empty.
 * @property operator The operator to apply in the RADIUS reply for this entry; it must not be empty.
 * @property value The value to be used in the reply for this entry; it must not be empty.
 */
@Entity
@Table(name = "radgroupreply")
data class RadGroupReplyEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @Column(name = "groupname")
  @field:NotEmpty(message = "{empty.groupName}")
  var groupName: String? = null,

  @Column(name = "attribute")
  @field:NotEmpty(message = "{empty.attribute}")
  var attribute: String? = null,

  @Column(name = "op")
  @field:NotEmpty(message = "{empty.operator}")
  var operator: String? = null,

  @Column(name = "\"value\"")
  @field:NotEmpty(message = "{empty.value}")
  var value: String? = null
)