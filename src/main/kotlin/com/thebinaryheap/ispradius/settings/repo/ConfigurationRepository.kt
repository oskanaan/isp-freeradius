package com.thebinaryheap.ispradius.settings.repo

import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.common.errors.UniqueValueException
import com.thebinaryheap.ispradius.radius.validation.ValidLongitudeLatitude
import com.thebinaryheap.ispradius.settings.validation.ValidCronPattern
import com.thebinaryheap.ispradius.settings.validation.ValidRegex
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.QueryHint
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import org.hibernate.jpa.AvailableHints
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Persistable
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

/**
 * Repository for CRUD operations on configuration settings.
 */
@Repository
interface ConfigurationRepository : CrudRepository<ConfigurationSettings, Long>

/**
 * Repository for CRUD operations on city entities.
 */
@RepositoryRestResource(collectionResourceRel = "cities", path = "cities")
interface CityRepository : CrudRepository<City, Long> {
  @QueryHints(QueryHint(name = AvailableHints.HINT_FLUSH_MODE, value = "COMMIT"))
  fun countCitiesByNameEqualsIgnoreCase(name: String): Long
  fun findAllByOrderByNameAsc(): List<City>
}

/**
 * Entity representing configuration settings with validations for regex, CRON pattern, and coordinates.
 */
@Entity
data class ConfigurationSettings (
  @Id
  var id: Long = 1,

  @Column(nullable = false)
  @NotEmpty(message = "A phone regular expression must be provided.")
  @ValidRegex
  var phoneRegex: String? = null,

  @Column(nullable = false)
  @NotEmpty(message = "A schedule CRON pattern must be provided.")
  @ValidCronPattern
  var cronPattern: String? = null,

  @Column(nullable = false)
  @NotEmpty(message = "A default longitude must be provided.")
  @ValidLongitudeLatitude
  var defaultLongitude: String? = null,

  @Column(nullable = false)
  @NotEmpty(message = "A default latitude must be provided.")
  @ValidLongitudeLatitude
  var defaultLatitude: String? = null
) : Persistable<Long> {
  override fun getId(): Long {
    return id
  }

  override fun isNew(): Boolean {
    return false
  }

  override fun toString(): String {
    return "ConfigurationSettings(id=$id, phoneRegex=$phoneRegex, cronPattern=$cronPattern, defaultLongitude=$defaultLongitude, defaultLatitude=$defaultLatitude)"
  }
}

/**
 * Entity representing a city with a unique name constraint.
 */
@Entity
@EntityListeners(CityEntityListener::class)
@Table(name = "cities")
data class City(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @field:NotEmpty(message = "A city name must be specified.")
  var name: String = ""
)

/**
 * Entity listener for City to enforce the uniqueness of city names.
 */
class CityEntityListener {
  @PrePersist
  fun prePersist(city: City) {
    validateUniqueCityName(city.name)
  }

  @PreUpdate
  fun preUpdate(city: City) {
    validateUniqueCityName(city.name)
  }

  private fun validateUniqueCityName(name: String?) {
    val uniqueCityNameValidator = ApplicationContextProvider.applicationContext?.getBean(UniqueCityNameValidator::class.java)!!
    if (!uniqueCityNameValidator.isValid(name)) {
      val messageSource = ApplicationContextProvider.applicationContext?.getBean(MessageSource::class.java)
      val message = messageSource?.getMessage("error.cityName.not.unique", null, LocaleContextHolder.getLocale()) ?: "City name must be unique."
      throw UniqueValueException("name", message)
    }
  }
}

/**
 * Component to validate the uniqueness of a city's name.
 */
@Component
class UniqueCityNameValidator {
  private var log: Logger = LoggerFactory.getLogger(UniqueCityNameValidator::class.java)

  @Autowired
  lateinit var cityRepository: CityRepository

  fun isValid(cityName: String?): Boolean {
    log.debug("Validating that city name '$cityName' is unique.")

    if (!cityName.isNullOrBlank() && cityRepository.countCitiesByNameEqualsIgnoreCase(cityName) > 0) {
      log.error("Name '$cityName' is not unique, validation failed.")
      return false
    }

    return true
  }
}
