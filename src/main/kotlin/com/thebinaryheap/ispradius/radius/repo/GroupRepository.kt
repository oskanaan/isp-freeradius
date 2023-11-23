package com.thebinaryheap.ispradius.radius.repo

import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.common.errors.UniqueValueException
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
import java.math.BigDecimal
import java.util.*
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
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.stereotype.Component


/**
 * Repository interface for accessing and managing group data.
 * Supports pagination and sorting of group entities.
 */
@RepositoryRestResource(collectionResourceRel = "groups", path = "groups")
interface GroupRepository : PagingAndSortingRepository<GroupEntity, Long>, CrudRepository<GroupEntity, Long> {
  @Query("SELECT g FROM GroupEntity g WHERE " +
      " :groupName IS NULL OR :groupName = '' OR LOWER(g.groupName) = LOWER(:groupName) " +
      "ORDER BY g.groupName")
  fun findByGroupNameIgnoreCase(groupName: String, pageable: Pageable): Page<GroupEntity>

  @QueryHints(QueryHint(name = AvailableHints.HINT_FLUSH_MODE, value = "COMMIT"))
  fun findByGroupName(groupName: String?): List<GroupEntity>
}


/**
 * Component for validating the uniqueness of group names within the group repository.
 */
@Component
class UniqueGroupNameValidator {
  private var log: Logger = LoggerFactory.getLogger(UniqueGroupNameValidator::class.java)

  @Autowired
  lateinit var groupRepository: GroupRepository

  fun isValid(group: GroupEntity): Boolean {
    log.debug("Validating that groupname '${group.groupName}' is unique.")

    if (!group.groupName.isNullOrBlank()) {
      val groups = groupRepository.findByGroupName(group.groupName!!)
      if (groups.size > 1 || (groups.size == 1 && group.id != groups.first().id)) {
        log.error("Name '${group.groupName}' is not unique, validation failed.")
        return false
      }
    }

    return true
  }
}

/**
 * Entity listener for [GroupEntity] to handle validation of group name uniqueness
 * before persisting or updating, and cleaning up related data before removal.
 */
class GroupEntityListener {

  /**
   * Throws a [UniqueValueException] if the group name is not unique when a [GroupEntity] is being persisted.
   */
  @PrePersist
  fun prePersist(group: GroupEntity) {
    val uniqueGroupNameValidator = ApplicationContextProvider.applicationContext?.getBean(UniqueGroupNameValidator::class.java)
    if (uniqueGroupNameValidator != null && !uniqueGroupNameValidator.isValid(group)) {
      val messageSource = ApplicationContextProvider.applicationContext?.getBean(MessageSource::class.java)
      val message = messageSource?.getMessage("error.groupName.not.unique", null, LocaleContextHolder.getLocale()) ?: "Group name is already in use"

      throw UniqueValueException("groupName", message)
    }
  }


  /**
   * Throws a [UniqueValueException] if the group name is not unique when a [GroupEntity] is being updated.
   */
  @PreUpdate
  fun preUpdate(group: GroupEntity) {
    val uniqueGroupNameValidator = ApplicationContextProvider.applicationContext?.getBean(UniqueGroupNameValidator::class.java)
    if (uniqueGroupNameValidator != null && !uniqueGroupNameValidator.isValid(group)) {
      val messageSource = ApplicationContextProvider.applicationContext?.getBean(MessageSource::class.java)
      val message = messageSource?.getMessage("error.groupName.not.unique", null, LocaleContextHolder.getLocale()) ?: "Group name is already in use"

      throw UniqueValueException("groupName", message)
    }
  }


  /**
   * Cleans up related data in [RadGroupCheckRepository] and [RadGroupReplyRepository] when a [GroupEntity] is removed.
   */
  @PreRemove
  fun preRemove(group: GroupEntity) {
    val radGroupCheckRepository = ApplicationContextProvider.applicationContext?.getBean(RadGroupCheckRepository::class.java)
    val radGroupReplyRepository = ApplicationContextProvider.applicationContext?.getBean(RadGroupReplyRepository::class.java)
    if (radGroupCheckRepository != null && radGroupReplyRepository != null && group.groupName != null) {
      radGroupCheckRepository.findByGroupName(group.groupName).forEach {
        radGroupCheckRepository.delete(it)
      }
      radGroupReplyRepository.findByGroupName(group.groupName).forEach {
        radGroupReplyRepository.delete(it)
      }
    }
  }
}


/**
 * Entity representing a group, mapping to the 'groups' table in the database.
 *
 * @property id The primary key identifier of the group.
 * @property groupName The unique name of the group.
 * @property price The price associated with the group, defaults to zero.
 */
@Entity
@EntityListeners(GroupEntityListener::class)
@Table(name = "\"groups\"")
data class GroupEntity(
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  var id: Int? = null,
  @Basic
  @field:NotEmpty(message = "{empty.groupName}")
  @Column(name = "groupname")
  var groupName: String? = null,
  @Basic
  @Column(name = "price")
  var price: BigDecimal? = BigDecimal.ZERO)