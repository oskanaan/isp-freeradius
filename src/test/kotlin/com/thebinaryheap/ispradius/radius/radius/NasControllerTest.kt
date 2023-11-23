package com.thebinaryheap.ispradius.radius.radius

import com.fasterxml.jackson.databind.ObjectMapper
import com.thebinaryheap.ispradius.TestRestConfiguration
import com.thebinaryheap.ispradius.common.controller.ExceptionHandler
import com.thebinaryheap.ispradius.radius.repo.NasEntity
import com.thebinaryheap.ispradius.radius.repo.NasRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = [TestRestConfiguration::class])
@Import(ExceptionHandler::class)
class NasControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var nasRepository: NasRepository

  @BeforeEach
  fun setUp() {
    nasRepository.deleteAll()
  }

  @Test
  fun `should return all NAS entities`() {
    val nasEntities = listOf(
      NasEntity(nasName = "NAS1", shortName = "Short name", secret = "secret1"),
      NasEntity(nasName = "NAS2", shortName = "Short name", secret = "secret2")
    )
    nasEntities.forEach {nasRepository.save(it)}

    mockMvc.perform(get("/nas"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$._embedded.nas[0].nasName").value("NAS1"))
      .andExpect(jsonPath("$._embedded.nas[1].nasName").value("NAS2"))
  }

  @Test
  fun `should create a new NAS entity`() {
    val newNas = NasEntity(nasName = "NewNAS", shortName = "Short name", secret = "new_secret")

    mockMvc.perform(post("/nas")
      .contentType(MediaType.APPLICATION_JSON)
      .content(ObjectMapper().writeValueAsString(newNas)))
      .andExpect(status().isCreated)
  }

  @Test
  fun `should update an existing NAS entity`() {
    val existingNas = NasEntity(id = 1, nasName = "ExistingNAS", shortName = "Short name", secret = "existing_secret")
    nasRepository.save(existingNas)
    val updatedNas = NasEntity(id = 1, nasName = "UpdatedNAS", shortName = "Short name", secret = "updated_secret")

    mockMvc.perform(put("/nas/{id}", existingNas.id)
      .contentType(MediaType.APPLICATION_JSON)
      .content(ObjectMapper().writeValueAsString(updatedNas)))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.nasName").value("UpdatedNAS"))
  }

  @Test
  fun `should delete an existing NAS entity`() {
    val nasId = nasRepository.save(NasEntity(id = 1, nasName = "ExistingNAS", shortName = "Short name", secret = "existing_secret")).id
    mockMvc.perform(delete("/nas/{id}", nasId))
      .andExpect(status().isOk)
  }

  @Test
  fun `should return 404 when NAS entity not found for get by ID`() {
    val nasId = 999L // Non-existent ID

    mockMvc.perform(get("/nas/{id}", nasId))
      .andExpect(status().isNotFound)
  }

  @Test
  fun `should return 400 when creating a NAS entity with missing required fields`() {
    val invalidNas = NasEntity(nasName = null, secret = null)

    mockMvc.perform(post("/nas")
      .contentType(MediaType.APPLICATION_JSON)
      .content(ObjectMapper().writeValueAsString(invalidNas)))
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `should return 201 when updating a NAS entity with non-existent ID`() {
    val nonExistentNas = NasEntity(id = 999L, nasName = "NonExistentNAS", shortName = "Short name", secret = "secret")

    mockMvc.perform(put("/nas/{id}", nonExistentNas.id)
      .contentType(MediaType.APPLICATION_JSON)
      .content(ObjectMapper().writeValueAsString(nonExistentNas)))
      .andExpect(status().isCreated)
  }

  @Test
  fun `should return 404 when deleting a NAS entity with non-existent ID`() {
    val nasId = 999L // Non-existent ID

    mockMvc.perform(delete("/nas/{id}", nasId))
      .andExpect(status().isNotFound)
  }
}
