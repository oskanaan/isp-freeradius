package com.thebinaryheap.ispradius.radius.repo

import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.common.errors.ManagedAttributeException
import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import jakarta.persistence.PreUpdate
import jakarta.persistence.QueryHint
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import org.hibernate.jpa.AvailableHints
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource


/**
 * Repository interface for managing Radcheck entities, with methods to find, delete and search these entities based on different criteria.
 */
@RepositoryRestResource(collectionResourceRel = "radcheck", path = "radcheck")
interface RadcheckRepository : PagingAndSortingRepository<RadcheckEntity, Long>, CrudRepository<RadcheckEntity, Long> {
  fun findByUsernameIgnoreCase(username: String?, pageable: Pageable): Page<RadcheckEntity>
  fun findByUsername(username: String?): List<RadcheckEntity>

  @QueryHints(QueryHint(name = AvailableHints.HINT_FLUSH_MODE, value = "COMMIT"))
  fun findByUsernameAndAttribute(username: String, attribute: String): List<RadcheckEntity>

  fun deleteByUsername(username: String)

  @Query("SELECT r FROM RadcheckEntity r WHERE r.username NOT IN " +
      "(SELECT u.username FROM RadUserGroupEntity u WHERE u.groupName = :groupName) AND LOWER(r.username) like LOWER(CONCAT('%', :username, '%'))")
  fun findUsersNotInGroup(@Param("username") username: String?, @Param("groupName") groupName: String, pageable: Pageable): Page<RadcheckEntity>

}

/**
 * Entity representing a radcheck, which is an entry for RADIUS authentication and authorization.
 *
 * @property id The unique identifier of the radcheck.
 * @property username The username associated with this radcheck entry, must not be empty.
 * @property attribute The RADIUS attribute associated with this radcheck entry, must not be empty.
 * @property operator The operator to be used in the check for this entry, must not be empty.
 * @property value The value associated with the attribute for this entry, must not be empty.
 * @property internalEdit Indicates whether the operation was initiated internally, bypassing certain checks.
 */
class RadCheckEntityListener {

  private val managedAttributes = listOf("SHA-256-Password", "Cleartext-Password", "User-Password", "Crypt-Password",
    "MD5-Password", "SHA1-Password", "SHA2-Password", "SHA2-224-Password", "SHA2-256-Password", "SHA2-384-Password", "SHA2-512-Password",
    "SMD5-Password", "SSHA-Password", "NT-Password", "Auth-Type").map { it.lowercase() }

  private val managedPasswordErrorCode = "error.managed.password.change"

  @PrePersist
  fun prePersist(radcheck: RadcheckEntity) {
    if (managedAttributes.contains(radcheck.attribute?.lowercase()) && !radcheck.internalEdit) {
      val messageSource = ApplicationContextProvider.applicationContext?.getBean(MessageSource::class.java)
      val message = messageSource?.getMessage(managedPasswordErrorCode, null, LocaleContextHolder.getLocale()) ?: "To change a password or Auth-Type attribute, edit the user's client record"

      throw ManagedAttributeException("attribute", message)
    }
  }

  @PreUpdate
  fun preUpdate(radcheck: RadcheckEntity) {
    if (managedAttributes.contains(radcheck.attribute?.lowercase()) && !radcheck.internalEdit) {
      val messageSource = ApplicationContextProvider.applicationContext?.getBean(MessageSource::class.java)
      val message = messageSource?.getMessage(managedPasswordErrorCode, null, LocaleContextHolder.getLocale()) ?: "To change a password or Auth-Type attribute, edit the user's client record"

      throw ManagedAttributeException("attribute", message)
    }
  }

  @PreRemove
  fun preRemove(radcheck: RadcheckEntity) {
    if (managedAttributes.contains(radcheck.attribute?.lowercase()) && !radcheck.internalEdit) {
      val messageSource = ApplicationContextProvider.applicationContext?.getBean(MessageSource::class.java)
      val message = messageSource?.getMessage(managedPasswordErrorCode, null, LocaleContextHolder.getLocale()) ?: "To change a password or Auth-Type attribute, edit the user's client record"

      throw ManagedAttributeException("attribute", message)
    }
  }
}

@Entity
@EntityListeners(RadCheckEntityListener::class)
@Table(name = "radcheck")
data class RadcheckEntity(
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  var id:Int  = 0,

  @Basic
  @Column(name = "username")
  @field:NotEmpty(message = "{empty.username}")
  var username: String? = null,

  @Basic
  @Column(name = "attribute")
  @field:NotEmpty(message = "{empty.attribute}")
  var attribute: String? = null,

  @Basic
  @Column(name = "op")
  @field:NotEmpty(message = "{empty.operator}")
  var operator: String? = null,

  @Basic
  @Column(name = "\"value\"")
  @field:NotEmpty(message = "{empty.value}")
  var value: String? = null,

  //Indicates that the operation was initiated by an internal call, which would bypass certain checks before insert/update/delete.
  @Transient
  var internalEdit: Boolean = false

)
{

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val that = other as RadcheckEntity?

    return id == that!!.id
  }

  override fun hashCode(): Int {
    var result = id
    result = 31 * result + if (username != null) username!!.hashCode() else 0
    result = 31 * result + if (attribute != null) attribute!!.hashCode() else 0
    result = 31 * result + if (operator != null) operator!!.hashCode() else 0
    result = 31 * result + if (value != null) value!!.hashCode() else 0
    return result
  }
}
