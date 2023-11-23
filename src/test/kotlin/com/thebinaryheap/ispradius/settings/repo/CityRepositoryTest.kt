package com.thebinaryheap.ispradius.settings.repo

import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.common.controller.ExceptionHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(UniqueCityNameValidator::class, ApplicationContextProvider::class)
class CityRepositoryTest {

  @Autowired
  lateinit var cityRepository: CityRepository

  @Autowired
  lateinit var testEntityManager: TestEntityManager

  @BeforeEach
  fun clearCities() {
    cityRepository.deleteAll()
  }

  @Test
  fun `test save City`() {
    val city = City(name = "New York")
    cityRepository.save(city)
    val count = cityRepository.countCitiesByNameEqualsIgnoreCase("New York")
    assertEquals(1, count)
  }

  @Test
  fun `test find City with nonexistent name`() {
    val count = cityRepository.countCitiesByNameEqualsIgnoreCase("Nonexistent")
    assertEquals(0, count)
  }

  @Test
  fun `test delete City`() {
    var city = City(name = "New York")
    city = cityRepository.save(city)
    cityRepository.deleteById(city.id!!)
    testEntityManager.flush()
    val count = cityRepository.countCitiesByNameEqualsIgnoreCase("New York")
    assertEquals(0, count)
  }

}
