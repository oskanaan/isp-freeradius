package com.thebinaryheap.ispradius.settings.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.settings.repo.City
import com.thebinaryheap.ispradius.settings.repo.CityRepository
import com.thebinaryheap.ispradius.settings.repo.ConfigurationRepository
import com.thebinaryheap.ispradius.settings.repo.ConfigurationSettings
import jakarta.persistence.EntityManager
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(ConfigurationController::class)
class ConfigurationControllerTest {

  @MockBean
  private lateinit var configurationRepository: ConfigurationRepository

  @MockBean
  private lateinit var cityRepository: CityRepository

  @MockBean
  private lateinit var entityManager: EntityManager

  @Autowired
  private lateinit var webApplicationContext: WebApplicationContext

  @Autowired
  private lateinit var mockMvc: MockMvc

  @BeforeEach
  fun setUp() {
    Mockito.reset(configurationRepository, cityRepository, entityManager)
    MockitoAnnotations.openMocks(this)
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    ApplicationContextProvider.applicationContext = webApplicationContext
  }

  @Test
  fun `test GET configurationSettings`() {
    val settings = ConfigurationSettings(
      phoneRegex = "\\d+",
      cronPattern = "0 0 12 * * ?",
      defaultLongitude = "12.34",
      defaultLatitude = "56.78"
    )
    `when`(configurationRepository.count()).thenReturn(1L)
    `when`(configurationRepository.findAll()).thenReturn(listOf(settings))

    mockMvc.perform(get("/configuration/settings"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.phoneRegex").value("\\d+"))
  }

  @Test
  fun `test PUT updateConfigurationSettings`() {
    val settings = ConfigurationSettings(
      phoneRegex = "\\d+",
      cronPattern = "0 0 12 * * ?",
      defaultLongitude = "12.34",
      defaultLatitude = "56.78"
    )
    `when`(configurationRepository.count()).thenReturn(1L)

    val json = ObjectMapper().writeValueAsString(settings)

    mockMvc.perform(put("/configuration/settings")
      .contentType(MediaType.APPLICATION_JSON)
      .content(json))
      .andExpect(status().isNoContent)
  }

  @Test
  fun `test PUT updateConfigurationSettings with invalid data`() {
    val settings = ConfigurationSettings(
      phoneRegex = "", // invalid
      cronPattern = "", // invalid
      defaultLongitude = "",
      defaultLatitude = ""
    )
    val json = ObjectMapper().writeValueAsString(settings)

    mockMvc.perform(put("/configuration/settings")
      .contentType(MediaType.APPLICATION_JSON)
      .content(json))
      .andExpect(status().isBadRequest)

    verify(configurationRepository, times(0)).save(any())
  }
}
