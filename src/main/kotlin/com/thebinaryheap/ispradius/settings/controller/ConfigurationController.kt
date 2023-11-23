package com.thebinaryheap.ispradius.settings.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.thebinaryheap.ispradius.settings.Settings
import com.thebinaryheap.ispradius.settings.repo.CityRepository
import com.thebinaryheap.ispradius.settings.repo.ConfigurationRepository
import com.thebinaryheap.ispradius.settings.repo.ConfigurationSettings
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Handles retrieving and updating the application global configuration. This includes:
 *
 * - Invoice generation cron pattern
 * - Phone number regular expression
 */
@RestController
@RequestMapping("/configuration")
class ConfigurationController(
  private val configurationRepository: ConfigurationRepository,
  private val cityRepository: CityRepository
)
{

  private var log: Logger = LoggerFactory.getLogger(ConfigurationController::class.java)

  @GetMapping("/settings")
  fun configurationSettings() : ResponseEntity<ConfigurationSettings> {
    return if (configurationRepository.count() == 0L) {
      log.debug("No configuration settings found, populating defaults.")
      ResponseEntity<ConfigurationSettings>(
        ConfigurationSettings(
        cronPattern = Settings.cronPattern,
        phoneRegex = Settings.phoneRegex,
        defaultLatitude = Settings.defaultLatitude,
        defaultLongitude = Settings.defaultLongitude
      ), HttpStatus.OK)
    } else {
      val configurationSettings = configurationRepository.findAll().first()

      log.debug("Found the following configuration settings: ${ObjectMapper().writeValueAsString(configurationSettings)} .")
      ResponseEntity<ConfigurationSettings>(configurationSettings, HttpStatus.OK)
    }
  }

  @PutMapping("/settings")
  fun updateConfigurationSettings(@Valid @RequestBody configurationSettings: ConfigurationSettings) : ResponseEntity<Any> {
    val httpStatus = if (configurationRepository.count() == 0L) {
      HttpStatus.CREATED
    } else {
      HttpStatus.NO_CONTENT
    }

    log.debug("About to save the configuration settings: ${ObjectMapper().writeValueAsString(configurationSettings)}")
    configurationRepository.save(configurationSettings)
    return ResponseEntity(httpStatus)
  }
}