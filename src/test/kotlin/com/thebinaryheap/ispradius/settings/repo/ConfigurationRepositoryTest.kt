package com.thebinaryheap.ispradius.settings.repo

import jakarta.validation.ConstraintViolationException
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class ConfigurationRepositoryTest {

    @Autowired
    lateinit var configurationRepository: ConfigurationRepository

    @Test
    fun `test save ConfigurationSettings`() {
        val settings = ConfigurationSettings(
            id = 1,
            phoneRegex = "\\d+",
            cronPattern = "0 0 12 * * ?",
            defaultLongitude = "12.34",
            defaultLatitude = "56.78"
        )
        configurationRepository.save(settings)
        val found = configurationRepository.findById(1L)
        assertEquals(settings, found.orElse(null))
    }

    @Test
    fun `test find ConfigurationSettings by ID`() {
        val settings = ConfigurationSettings(
            id = 3,
            phoneRegex = "\\d+",
            cronPattern = "0 0 12 * * ?",
            defaultLongitude = "12.34",
            defaultLatitude = "56.78"
        )
        configurationRepository.save(settings)
        val found = configurationRepository.findById(3L)
        assertEquals(settings, found.orElse(null))
    }

    @Test
    fun `test delete ConfigurationSettings`() {
        val settings = ConfigurationSettings(
            id = 4,
            phoneRegex = "\\d+",
            cronPattern = "0 0 12 * * ?",
            defaultLongitude = "12.34",
            defaultLatitude = "56.78"
        )
        configurationRepository.save(settings)
        configurationRepository.deleteById(4L)
        val found = configurationRepository.findById(4L)
        assertEquals(Optional.empty<ConfigurationSettings>(), found)
    }

}
