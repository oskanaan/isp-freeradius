package com.thebinaryheap.ispradius.settings

import com.thebinaryheap.ispradius.settings.repo.ConfigurationRepository
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

/**
 * A service which caches application settings on startup.
 */
@Service
@Scope("singleton")
class Settings {

  private var log: Logger = LoggerFactory.getLogger(Settings::class.java)

  @Autowired
  private lateinit var configurationRepository: ConfigurationRepository
  @PostConstruct
  fun postConstruct() {
    if (configurationRepository.count() > 0L) {
      val configuration = configurationRepository.findAll().first()
      log.debug("Found an existing settings record, populating cached settings from it. [${configuration}].")

      val regex = configuration.phoneRegex
      if (regex != null) {
        phoneRegex = regex
      }

      val cron = configuration.cronPattern
      if (cron != null) {
        cronPattern = cron
      }

      val logitude = configuration.defaultLongitude

      if (logitude != null) {
        defaultLongitude = logitude
      }

      val latitude = configuration.defaultLatitude

      if (latitude != null) {
        defaultLatitude = latitude
      }
    }
  }

  companion object {
    var phoneRegex: String = "^(([\\+]?[0-9]{3}[0-9]{1})|[0-9]{2})[0-9]{6}\$"
    var cronPattern: String = "0 10 10 28-31 * ?"
    var defaultLongitude: String = "35.7614"
    var defaultLatitude: String = "33.5993"
  }
}