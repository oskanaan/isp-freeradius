package com.thebinaryheap.ispradius.radius.radius

import com.fasterxml.jackson.databind.ObjectMapper
import com.thebinaryheap.ispradius.TestRestConfiguration
import com.thebinaryheap.ispradius.common.controller.ExceptionHandler
import com.thebinaryheap.ispradius.radius.repo.RadGroupReplyEntity
import com.thebinaryheap.ispradius.radius.repo.RadGroupReplyRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
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
@Import(ExceptionHandler::class)
class RadGroupReplyControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var radGroupReplyRepository: RadGroupReplyRepository

  @Test
  fun `should retrieve a RadGroupReplyEntity by ID`() {
    val radGroupReply = RadGroupReplyEntity(
      groupName = "TestGroup",
      attribute = "TestAttribute",
      operator = "TestOperator",
      value = "TestValue"
    )
    val radGroupReplyId = radGroupReplyRepository.save(radGroupReply).id

    mockMvc.perform(get("/radgroupreply/{id}", radGroupReplyId))
      .andExpect(status().isOk)
  }

  @Test
  fun `should create a new RadGroupReplyEntity`() {
    val radGroupReply = RadGroupReplyEntity(
      groupName = "TestGroup",
      attribute = "TestAttribute",
      operator = "TestOperator",
      value = "TestValue"
    )

    mockMvc.perform(
      post("/radgroupreply")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.groupName").value("TestGroup"))
  }

  @Test
  fun `should update an existing RadGroupReplyEntity`() {
    val radGroupReply = RadGroupReplyEntity(
      groupName = "TestGroup",
      attribute = "TestAttribute",
      operator = "TestOperator",
      value = "TestValue"
    )
    val radGroupReplyId = radGroupReplyRepository.save(radGroupReply).id

    val updatedRadGroupReply = RadGroupReplyEntity(
      id = radGroupReplyId,
      groupName = "UpdatedGroup",
      attribute = "UpdatedAttribute",
      operator = "UpdatedOperator",
      value = "UpdatedValue"
    )

    mockMvc.perform(
      put("/radgroupreply/{id}", radGroupReplyId)
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(updatedRadGroupReply))
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.groupName").value("UpdatedGroup"))
  }

  @Test
  fun `should delete an existing RadGroupReplyEntity`() {
    val radGroupReplyId = 1L
    val radGroupReply = RadGroupReplyEntity(
      groupName = "TestGroup",
      attribute = "TestAttribute",
      operator = "TestOperator",
      value = "TestValue"
    )
    radGroupReplyRepository.save(radGroupReply)

    mockMvc.perform(delete("/radgroupreply/{id}", radGroupReplyId))
      .andExpect(status().isOk)
  }

  @Test
  fun `should return bad request when creating RadGroupReplyEntity with missing groupName`() {
    val radGroupReply = RadGroupReplyEntity(
      attribute = "TestAttribute",
      operator = "TestOperator",
      value = "TestValue"
    )

    mockMvc.perform(
      post("/radgroupreply")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].message").value("{empty.groupName}"))
  }

  @Test
  fun `should return bad request when creating RadGroupReplyEntity with missing attribute`() {
    val radGroupReply = RadGroupReplyEntity(
      groupName = "TestGroup",
      operator = "TestOperator",
      value = "TestValue"
    )

    mockMvc.perform(
      post("/radgroupreply")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].message").value("{empty.attribute}"))
  }

  @Test
  fun `should return bad request when creating RadGroupReplyEntity with missing operator`() {
    val radGroupReply = RadGroupReplyEntity(
      groupName = "TestGroup",
      attribute = "TestAttribute",
      value = "TestValue"
    )

    mockMvc.perform(
      post("/radgroupreply")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].message").value("{empty.operator}"))
  }

  @Test
  fun `should return bad request when creating RadGroupReplyEntity with missing value`() {
    val radGroupReply = RadGroupReplyEntity(
      groupName = "TestGroup",
      attribute = "TestAttribute",
      operator = "TestOperator"
    )

    mockMvc.perform(
      post("/radgroupreply")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].message").value("{empty.value}"))
  }
}
