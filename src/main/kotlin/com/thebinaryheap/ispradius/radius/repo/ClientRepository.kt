package com.thebinaryheap.ispradius.radius.repo

import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.radius.validation.ValidLongitudeLatitude
import com.thebinaryheap.ispradius.radius.validation.ValidPhoneNumber
import com.thebinaryheap.ispradius.settings.repo.City
import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EntityManager
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PostLoad
import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.stereotype.Component

/**
 * Repository for accessing and manipulating client data.
 * Extends PagingAndSortingRepository for pagination and sorting capabilities.
 */
@RepositoryRestResource(collectionResourceRel = "clients", path = "clients")
interface ClientRepository : PagingAndSortingRepository<ClientEntity, Long>, CrudRepository<ClientEntity, Long> {

  @Query("SELECT c FROM ClientEntity c WHERE " +
      "(:fullName IS NULL OR :fullName = '' OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
      "AND (:status IS NULL OR c.status = :status) " +
      "AND (:username IS NULL OR :username = '' OR LOWER(c.username) = LOWER(:username)) " +
      "AND (:phone IS NULL OR :phone = '' OR LOWER(c.phoneNumber) = LOWER(:phone)) " +
      "ORDER BY c.fullName")
  fun searchClients(@Param("fullName") fullName: String?,
                    @Param("status") status: ClientStatus?,
                    @Param("username") username: String?,
                    @Param("phone") phone: String?,
                    pageable: Pageable): Page<ClientEntity>

  fun findByStatus(status: ClientStatus): List<ClientEntity>
  fun findByUsernameIgnoreCase(username: String): List<ClientEntity>
  fun findByFullNameContainsIgnoreCase(fullName: String): List<ClientEntity>
}

/**
 * Validator component to ensure the uniqueness of a username within the client repository.
 */
@Component
class UniqueUsernameValidator {
  private var log: Logger = LoggerFactory.getLogger(UniqueUsernameValidator::class.java)

  @Autowired
  lateinit var clientRepository: ClientRepository

  /**
   * Validates that a username is unique.
   *
   * @param username The username to validate.
   * @return True if the username is unique, false otherwise.
   */
  fun isValid(username: String?): Boolean {
    log.debug("Validating that username '$username' is unique.")

    if (username.isNullOrBlank() || clientRepository.findByUsernameIgnoreCase(username).isNotEmpty()) {
      log.error("Name '$username' is not unique, validation failed.")
      return false
    }

    return true
  }
}

/**
 * Entity listener for ClientEntity to handle additional processing such as
 * creating or updating associated RAD check records before persisting or updating,
 * and cleaning up RAD check records before removal of a ClientEntity.
 */
class ClientEntityListener {
  private var log: Logger = LoggerFactory.getLogger(ClientEntityListener::class.java)

  private val passwordAttribute = "MD5-Password"
  private val authTypeAttribute = "Auth-Type"
  private val authTypeAccept = "Accept"
  private val authTypeReject = "Reject"


  @PrePersist
  fun prePersist(client: ClientEntity) {
    val radcheckRepository = ApplicationContextProvider.applicationContext?.getBean(RadcheckRepository::class.java)
    if (radcheckRepository != null && client.username != null && client.password != null) {
      //Create a radcheck password record
      val existingRecord = radcheckRepository.findByUsernameAndAttribute(client.username!!, passwordAttribute)
      var passwordRecord = if (existingRecord.isNotEmpty()) {
        existingRecord.first() //For some reason there is an existing record for this username, update it with the new password.
      } else {
        RadcheckEntity(username = client.username, attribute = passwordAttribute, operator = ":=")
      }
      passwordRecord.value = md5(client.password!!)

      passwordRecord.internalEdit = true
      radcheckRepository.save(passwordRecord)

      //Create a radcheck auth type record
      val authTypeRecord = RadcheckEntity(username = client.username, attribute = authTypeAttribute, operator = ":=")
      authTypeRecord.value = if (client.status == ClientStatus.ACTIVE) {
        authTypeAccept
      } else {
        authTypeReject
      }

      authTypeRecord.internalEdit = true
      radcheckRepository.save(authTypeRecord)
    }
  }

  @PreUpdate
  fun preUpdate(client: ClientEntity) {
    if (client.isBeingUpdated) {
      return
    }

    val radcheckRepository = ApplicationContextProvider.applicationContext?.getBean(RadcheckRepository::class.java)
    if (radcheckRepository != null && client.username != null && client.password != null) {
      val existingRecord = radcheckRepository.findByUsernameAndAttribute(client.username!!, passwordAttribute)
      if (existingRecord.isNotEmpty()) {
        client.isBeingUpdated = true
        val record = existingRecord.first()
        record.value = md5(client.password!!)
        val em = ApplicationContextProvider.applicationContext?.getBean(EntityManager::class.java)
        record.internalEdit = true
        em?.persist(record)

        //update the auth type record
        val authTypeRecords = radcheckRepository.findByUsernameAndAttribute(client.username!!, authTypeAttribute)
        //Should always be present, unless the data was changed manually which will cause problems
        if (authTypeRecords.isNotEmpty()) {
          val authTypeRecord = authTypeRecords.first()
          authTypeRecord.value = if (client.status == ClientStatus.ACTIVE) {
            authTypeAccept
          } else {
            authTypeReject
          }

          authTypeRecord.internalEdit = true
          em?.persist(authTypeRecord)
        } else {
          log.error("Could not find an attribute 'Auth-Type' for user ${client.username} to update its value.")
        }

        em?.flush()
        client.isBeingUpdated = false
      }
    }
  }

  @PreRemove
  fun preRemove(client: ClientEntity) {
    val radcheckRepository = ApplicationContextProvider.applicationContext?.getBean(RadcheckRepository::class.java)
    val radReplyRepository = ApplicationContextProvider.applicationContext?.getBean(RadReplyRepository::class.java)
    if (radcheckRepository != null && radReplyRepository != null && client.username != null) {
      radcheckRepository.findByUsername(client.username).forEach {
        it.internalEdit = true
        radcheckRepository.delete(it)
      }
      radReplyRepository.findByUsername(client.username).forEach {
        radReplyRepository.delete(it)
      }
    }
  }

  /**
   * Loads user groups. There is no foreign key relationship between a radcheck and radusergroups, so loading it manually
   * here on post-load. Since the client search is paged, the expectation is for it to have minimal performance impact.
   */
  @PostLoad
  fun postLoad(client: ClientEntity) {
    if (client.username != null) {
      val radUserGroupRepository = ApplicationContextProvider.applicationContext?.getBean(RadUserGroupRepository::class.java)!!
      client.radUserGroups = radUserGroupRepository.findByUsername(client.username!!)
    }
  }

  /**
   * Converts a string to its MD5 hash representation.
   *
   * @param input The input string to hash.
   * @return The MD5 hash of the input string.
   */
  private fun md5(input: String): String {
    val md = java.security.MessageDigest.getInstance("MD5")
    val hash = md.digest(input.toByteArray(charset("UTF-8")))
    return hash.fold("") { str, it -> str + "%02x".format(it) }
  }
}

/**
 * Entity representing a client. Maps to the 'clients' table.
 */
@Entity
@EntityListeners(ClientEntityListener::class)
@Table(name = "clients")
data class ClientEntity (
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  var id: Int? = null,

  @Basic
  @field:NotEmpty(message = "{empty.username}")
  @Column(name = "username")
  var username: String? = null,

  @Basic
  @field:NotEmpty(message = "{empty.password}")
  @Column(name = "password")
  var password: String? = null,

  @Basic
  @field:NotEmpty(message = "{empty.fullName}")
  @Column(name = "full_name")
  var fullName: String? = null,

  @Basic
  @Column(nullable = false)
  var status: ClientStatus = ClientStatus.ACTIVE,

  @Basic
  @ValidPhoneNumber
  @field:NotEmpty(message = "{empty.phone}")
  @Column(name = "phone_number")
  var phoneNumber: String? = null,

  @Basic
  @Column(name = "address_text")
  var addressText: String? = null,

  @Basic
  @ValidLongitudeLatitude
  @field:NotEmpty(message = "{empty.longitude}")
  @Column(name = "longitude")
  var longitude: String? = null,

  @Basic
  @ValidLongitudeLatitude
  @field:NotEmpty(message = "{empty.latitude}")
  @Column(name = "latitude")
  var latitude: String? = null,

  @Column(name = "city_id", nullable = false)
  @field:NotNull(message = "{empty.city}")
  var cityId: Long? = null,

  @ManyToOne
  @JoinColumn(name = "city_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
  var city: City? = null,

  @Enumerated(EnumType.STRING)
  @Column
  @field:NotNull(message = "{empty.subscriptionModel}")
  var subscriptionModel: ClientSubscriptionModel? = null,

  @Transient
  var radUserGroups: List<RadUserGroupEntity> = emptyList(),

  @Transient
  var isBeingUpdated: Boolean = false)
{
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this.javaClass != other.javaClass) return false

    other as ClientEntity

    if (id != other.id) return false
    if (fullName != other.fullName) return false
    if (phoneNumber != other.phoneNumber) return false
    if (addressText != other.addressText) return false
    if (longitude != other.longitude) return false
    if (latitude != other.latitude) return false

    return true
  }


  override fun hashCode(): Int {
    return if (id == null) {
      0
    } else {
      var result = id!!
      result = 31 * result + if (fullName != null) fullName.hashCode() else 0
      result = 31 * result + if (phoneNumber != null) phoneNumber.hashCode() else 0
      result = 31 * result + if (addressText != null) addressText.hashCode() else 0
      result = 31 * result + if (longitude != null) longitude.hashCode() else 0
      result = 31 * result + if (latitude != null) latitude.hashCode() else 0
      result
    }
  }
}

/**
 * Enum representing the status of a client.
 */
enum class ClientStatus {
  ACTIVE,
  SUSPENDED
}

/**
 * Enum representing the subscription model of a client.
 */
enum class ClientSubscriptionModel {
  MONTHLY,
  QUARTERLY,
  YEARLY
}
