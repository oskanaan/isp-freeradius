package com.thebinaryheap.ispradius.radius.repo

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

/**
 * Repository interface for accessing and managing NAS (Network Access Server) entities.
 * Supports pagination and sorting.
 */
@RepositoryRestResource(collectionResourceRel = "nas", path = "nas")
interface NasRepository : PagingAndSortingRepository<NasEntity, Long>, CrudRepository<NasEntity, Long> {
  @Query(
    "SELECT n FROM NasEntity n WHERE " +
        "(:nasName IS NULL OR LOWER(n.nasName) LIKE LOWER(CONCAT('%', :nasName, '%'))) AND " +
        "(:type IS NULL OR LOWER(n.type) LIKE LOWER(CONCAT('%', :type, '%'))) AND " +
        "(:server IS NULL OR LOWER(n.server) LIKE LOWER(CONCAT('%', :server, '%'))) AND " +
        "(:description IS NULL OR LOWER(n.description) LIKE LOWER(CONCAT('%', :description, '%')))"
  )
  fun searchNas(
    @Param("nasName") nasName: String?,
    @Param("type") type: String?,
    @Param("server") server: String?,
    @Param("description") description: String?,
    pageable: Pageable
  ): Page<NasEntity>
}

/**
 * Entity representing a NAS (Network Access Server).
 *
 * @property id The unique identifier of the NAS.
 * @property nasName The name of the NAS, must not be empty.
 * @property shortName A short name for the NAS, must not be empty.
 * @property type The type of the NAS.
 * @property ports The number of ports on the NAS.
 * @property secret A shared secret for the NAS, must not be empty.
 * @property server The server address for the NAS.
 * @property community The community string for the NAS.
 * @property description A description of the NAS.
 */
@Entity
@Table(name = "nas")
data class NasEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "nasname")
  @field:NotEmpty(message = "{empty.nasName}")
  var nasName: String? = null,

  @Column(name = "shortname")
  @field:NotEmpty(message = "{empty.shortName}")
  var shortName: String? = null,

  @Column(name = "type")
  var type: String? = null,

  @Column(name = "ports")
  @field:Max(65535)
  var ports: Int? = null,

  @Column(name = "secret")
  @field:NotEmpty(message = "{empty.secret}")
  var secret: String? = null,

  @Column(name = "server")
  var server: String? = null,

  @Column(name = "community")
  var community: String? = null,

  @Column(name = "description")
  var description: String? = null
)