package com.thebinaryheap.ispradius.radius.repo

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for CRUD operations on dictionary entries.
 * Supports pagination and sorting.
 */
@Repository
interface DictionaryRepository : PagingAndSortingRepository<Dictionary, Long> {
  fun findByAttributeContainingIgnoreCase(attribute: String?, pageable: Pageable): Page<Dictionary>
}

/**
 * Entity representing a dictionary record, mapping to the 'dictionary' table in the database.
 *
 * @property id The primary key of the dictionary record.
 * @property type The type of the dictionary record.
 * @property attribute The attribute of the dictionary record.
 * @property value The value associated with the dictionary attribute.
 * @property format The format of the dictionary record.
 * @property vendor The vendor associated with the dictionary record.
 * @property recommendedOP The recommended operation for this dictionary record.
 * @property recommendedTable The recommended table where this dictionary record is relevant.
 * @property recommendedHelper Any additional helper information for this dictionary record.
 * @property recommendedTooltip A longer description or tooltip for the dictionary record.
 */
@Entity
@Table(name = "dictionary")
data class Dictionary(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "Type")
  val type: String? = null,

  @Column(name = "Attribute")
  val attribute: String? = null,

  @Column(name = "Value")
  val value: String? = null,

  @Column(name = "Format")
  val format: String? = null,

  @Column(name = "Vendor")
  val vendor: String? = null,

  @Column(name = "RecommendedOP")
  val recommendedOP: String? = null,

  @Column(name = "RecommendedTable")
  val recommendedTable: String? = null,

  @Column(name = "RecommendedHelper")
  val recommendedHelper: String? = null,

  @Column(name = "RecommendedTooltip", length = 512)
  val recommendedTooltip: String? = null
)
