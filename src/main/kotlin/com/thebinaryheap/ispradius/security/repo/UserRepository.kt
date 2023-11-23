package com.thebinaryheap.ispradius.security.repo

import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.common.errors.ManagedAttributeException
import com.thebinaryheap.ispradius.common.errors.UniqueValueException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.QueryHint
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.jpa.AvailableHints
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * A repository interface for CRUD operations on user details.
 */
@RepositoryRestResource(collectionResourceRel = "users", path = "users")
interface UserRepository : CrudRepository<UserDetail, Long> {
  @QueryHints(QueryHint(name = AvailableHints.HINT_FLUSH_MODE, value = "COMMIT"))
  fun findByUsername(username: String): List<UserDetail>

  @Query("SELECT u FROM UserDetail u WHERE " +
      " (:username IS NULL OR :username = '' OR LOWER(u.username) = LOWER(:username)) " +
      " AND (:name IS NULL OR :name = '' OR LOWER(u.name) = LOWER(:name)) " +
      "ORDER BY u.name")
  fun searchUsers(username: String?, name: String?, pageable: Pageable): Page<UserDetail>
}

/**
 * Component to validate the uniqueness of a user's username.
 */
@Component
class UniqueUserUsernameValidator {
  private var log: Logger = LoggerFactory.getLogger(UniqueUserUsernameValidator::class.java)

  @Autowired
  lateinit var userRepository: UserRepository

  /**
   * Validates that a UserDetail's username is unique.
   *
   * @param userDetail The UserDetail to validate.
   * @return True if the username is unique, False otherwise.
   */
  fun isValid(userDetail: UserDetail): Boolean {
    log.debug("Validating that username '${userDetail.username}' is unique.")

    if (!userDetail.username.isNullOrBlank()) {
      val users = userRepository.findByUsername(userDetail.username!!)
      if (users.size > 1 || (users.size == 1 && userDetail.id != users.first().id)) {
        log.error("Name '${userDetail.username}' is not unique, validation failed.")
        return false
      }
    }

    return true
  }
}

/**
 * Entity listener for UserDetail to handle validation and password encoding.
 */
class UserDetailListener {
  private var log: Logger = LoggerFactory.getLogger(UserDetail::class.java)

  @PrePersist
  fun prePersist(userDetail: UserDetail) {
    val messageSource = ApplicationContextProvider.applicationContext?.getBean(MessageSource::class.java)
    val uniqueUsernameValidator = ApplicationContextProvider.applicationContext?.getBean(UniqueUserUsernameValidator::class.java)
    if (uniqueUsernameValidator != null && !uniqueUsernameValidator.isValid(userDetail)) {
      val message = messageSource?.getMessage("error.username.not.unique", null, LocaleContextHolder.getLocale()) ?: "Username is already in use."
      throw UniqueValueException("username", message)
    }

    val passwordEncoder = ApplicationContextProvider.applicationContext?.getBean(PasswordEncoder::class.java)
    if (userDetail.newPassword == null) {
      val message = messageSource?.getMessage("error.password.empty", null, LocaleContextHolder.getLocale()) ?: "A password must be provided"
      throw ManagedAttributeException("newPassword", message)
    }
    if (passwordEncoder != null) {
      userDetail.password = passwordEncoder.encode(userDetail.newPassword)
      userDetail.newPassword = null
    }
  }

  @PreUpdate
  fun preUpdate(userDetail: UserDetail) {
    val uniqueUsernameValidator = ApplicationContextProvider.applicationContext?.getBean(UniqueUserUsernameValidator::class.java)
    val messageSource = ApplicationContextProvider.applicationContext?.getBean(MessageSource::class.java)
    if (uniqueUsernameValidator != null && !uniqueUsernameValidator.isValid(userDetail)) {
      val message = messageSource?.getMessage("error.username.not.unique", null, LocaleContextHolder.getLocale()) ?: "Username is already in use."
      throw UniqueValueException("username", message)
    }

    val passwordEncoder = ApplicationContextProvider.applicationContext?.getBean(PasswordEncoder::class.java)
    if (passwordEncoder != null && userDetail.newPassword != null) {
      userDetail.password = passwordEncoder.encode(userDetail.newPassword)
      userDetail.newPassword = null
    }
  }

}



@Entity
@EntityListeners(UserDetailListener::class)
data class UserDetail(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @Column(nullable = false)
  @field:NotEmpty(message = "{empty.name}")
  var name: String? = null,

  @Column(nullable = false)
  @field:NotEmpty(message = "{empty.nationality}")
  var nationality: String? = null,

  @Column(nullable = false)
  @field:NotEmpty(message = "{empty.phone}")
  var phone: String? = null,

  var address: String? = null,

  @Column(nullable = false)
  var status: UserStatus = UserStatus.ACTIVE,

  @Column(nullable = false)
  @field:NotEmpty(message = "{empty.username}")
  @field:Size(min = 5, max = 15, message = "{error.invalid.username.size}")
  @field:Pattern(regexp = "^[a-zA-Z0-9._-]{5,}$", message = "{error.invalid.username.pattern}")
  var username: String? = null,

  @Column(nullable = false)
  var password: String? = null,

  @Column(nullable = true)
  @field:Size(min = 8, message = "{error.invalid.password.size}")
  @field:Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$=%^&*()_+]).{8,}$", message = "{error.invalid.password.pattern}")
  var newPassword: String? = null,

  @Column(nullable = false)
  @field:NotEmpty(message = "{empty.roles}")
  var roles: String? = null
)

enum class UserStatus {
  ACTIVE, SUSPENDED
}