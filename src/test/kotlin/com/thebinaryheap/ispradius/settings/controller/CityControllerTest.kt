package com.thebinaryheap.ispradius.settings.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.thebinaryheap.ispradius.TestRestConfiguration
import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.common.controller.ExceptionHandler
import com.thebinaryheap.ispradius.settings.repo.City
import com.thebinaryheap.ispradius.settings.repo.CityRepository
import com.thebinaryheap.ispradius.settings.repo.UniqueCityNameValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = [TestRestConfiguration::class])
@Import(ExceptionHandler::class, UniqueCityNameValidator::class, ApplicationContextProvider::class)
class CityControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var cityRepository: CityRepository

  @BeforeEach
  fun beforeEach() {
    cityRepository.deleteAll()
  }

  @Test
  fun `test GET cities when no cities exist`() {
    mockMvc.perform(get("/cities"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$._embedded.cities").isEmpty)
  }

  @Test
  fun `test DELETE city with invalid ID`() {
    mockMvc.perform(delete("/cities/999"))
      .andExpect(status().isNotFound)
  }

  @Test
  fun `test GET cities when cities exist`() {
    // Create and save some City entities in the repository
    val city1 = City(name = "City1")
    val city2 = City(name = "City2")
    cityRepository.saveAll(listOf(city1, city2))

    mockMvc.perform(get("/cities"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$._embedded.cities.length()").value(2))
      .andExpect(jsonPath("$._embedded.cities[0].name").value("City1"))
      .andExpect(jsonPath("$._embedded.cities[1].name").value("City2"))
  }

  @Test
  fun `test POST create city`() {
    val cityToCreate = City(name = "NewCity")

    mockMvc.perform(post("/cities")
      .contentType(MediaType.APPLICATION_JSON)
      .content(ObjectMapper().writeValueAsString(cityToCreate)))
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.name").value("NewCity"))

    // Verify that the city was added to the repository
    val createdCity = cityRepository.findAll().first()
    assertEquals("NewCity", createdCity.name)
  }

  @Test
  fun `test PUT update city`() {
    // Create and save a city in the repository
    val existingCity = City(name = "ExistingCity")
    val savedCity = cityRepository.save(existingCity)

    val updatedCity = City(id = savedCity.id, name = "UpdatedCity")

    mockMvc.perform(put("/cities/${savedCity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .content(ObjectMapper().writeValueAsString(updatedCity)))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.name").value("UpdatedCity"))

    // Verify that the city was updated in the repository
    val retrievedCity = cityRepository.findById(savedCity.id!!).orElse(null)
    assertNotNull(retrievedCity)
    assertEquals("UpdatedCity", retrievedCity.name)
  }

  @Test
  fun `test PUT update city with empty name should return a validation error`() {
    // Create and save a city in the repository
    val existingCity = City(name = "ExistingCity")
    val savedCity = cityRepository.save(existingCity)

    val updatedCity = City(id = savedCity.id)

    mockMvc.perform(put("/cities/${savedCity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .content(ObjectMapper().writeValueAsString(updatedCity)))
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].field").value("name"))
  }

  @Test
  fun `test DELETE city with valid ID`() {
    // Create and save a city in the repository
    val cityToDelete = City(name = "CityToDelete")
    val savedCity = cityRepository.save(cityToDelete)

    mockMvc.perform(delete("/cities/${savedCity.id}"))
      .andExpect(status().isOk)

    // Verify that the city was deleted from the repository
    val deletedCity = cityRepository.findById(savedCity.id!!)
    assertFalse(deletedCity.isPresent)
  }
}
