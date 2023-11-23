package com.thebinaryheap.ispradius.radius.radius

import com.fasterxml.jackson.databind.ObjectMapper
import com.thebinaryheap.ispradius.TestRestConfiguration
import com.thebinaryheap.ispradius.common.controller.ExceptionHandler
import com.thebinaryheap.ispradius.radius.repo.RadReplyEntity
import com.thebinaryheap.ispradius.radius.repo.RadReplyRepository
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.notNullValue
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
class RadReplyControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var radReplyRepository: RadReplyRepository

  @BeforeEach
  fun setup() {
    radReplyRepository.deleteAll()
  }

  @Test
  fun `should create a RadReplyEntity`() {
    val radReplyRequest = RadReplyEntity(username = "user1", attribute = "attr1", operator = "=", value = "value1")

    mockMvc.perform(
      post("/radreply")
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(radReplyRequest))
    )
      .andExpect(status().isCreated)
      .andExpect(header().string("Location", notNullValue()))
  }

  @Test
  fun `should retrieve a RadReplyEntity by ID`() {
    val radReply = RadReplyEntity(username = "user1", attribute = "attr1", operator = "=", value = "value1")
    val savedRadReply = radReplyRepository.save(radReply)

    mockMvc.perform(get("/radreply/{id}", savedRadReply.id))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.username").value(savedRadReply.username))
  }

  @Test
  fun `should update a RadReplyEntity`() {
    val radReply = RadReplyEntity(username = "user1", attribute = "attr1", operator = "=", value = "value1")
    val savedRadReply = radReplyRepository.save(radReply)
    val updatedRadReplyRequest = RadReplyEntity(username = "user2", attribute = "attr2", operator = "=", value = "value2")

    mockMvc.perform(
      put("/radreply/{id}", savedRadReply.id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(updatedRadReplyRequest))
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.username").value(updatedRadReplyRequest.username))
  }

  @Test
  fun `should delete a RadReplyEntity`() {
    val radReply = RadReplyEntity(username = "user1", attribute = "attr1", operator = "=", value = "value1")
    val savedRadReply = radReplyRepository.save(radReply)

    mockMvc.perform(delete("/radreply/{id}", savedRadReply.id))
      .andExpect(status().isOk)

    val deletedRadReply = radReplyRepository.findById(savedRadReply.id!!)
    assertThat(deletedRadReply).isEmpty()
  }

  @Test
  fun `should handle validation errors during entity creation`() {
    val invalidRadReplyRequest = RadReplyEntity(username = "", attribute = "", operator = "", value = "")

    mockMvc.perform(
      post("/radreply")
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(invalidRadReplyRequest))
    )
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `should handle not found error during retrieval`() {
    mockMvc.perform(get("/radreply/{id}", 99999))
      .andExpect(status().isNotFound)
  }
}
