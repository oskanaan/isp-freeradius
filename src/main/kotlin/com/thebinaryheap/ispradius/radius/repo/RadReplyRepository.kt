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
 * Repository interface for managing RadReply entities. It extends PagingAndSortingRepository and CrudRepository for providing pagination and CRUD operations.
 */
@RepositoryRestResource(collectionResourceRel = "radreply", path = "radreply")
interface RadReplyRepository : PagingAndSortingRepository<RadReplyEntity, Long>, CrudRepository<RadReplyEntity, Long> {
  fun findByUsernameIgnoreCase(username: String?, pageable: Pageable): Page<RadReplyEntity>
  fun findByUsername(username: String?): List<RadReplyEntity>
}

/**
 * Entity representing a radreply, which contains attributes for RADIUS replies based on username.
 *
 * @property id The unique identifier of the radreply entry, generated automatically.
 * @property username The username associated with this entry; it must not be empty as per validation constraints.
 * @property attribute The RADIUS attribute name for this entry; it cannot be empty and is validated accordingly.
 * @property operator The operator used in conjunction with the attribute and value; must not be empty and is validated.
 * @property value The value associated with the attribute for the RADIUS reply; it must not be empty and is validated.
 */
@Entity
@Table(name = "radreply")
data class RadReplyEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "username")
  @field:NotEmpty(message = "{empty.username}")
  var username: String? = null,

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