package com.thebinaryheap.ispradius.radius.radius

import com.fasterxml.jackson.databind.ObjectMapper
import com.thebinaryheap.ispradius.IspConfiguration
import com.thebinaryheap.ispradius.TestRestConfiguration
import com.thebinaryheap.ispradius.common.controller.ExceptionHandler
import com.thebinaryheap.ispradius.radius.repo.RadGroupCheckEntity
import com.thebinaryheap.ispradius.radius.repo.RadGroupCheckRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
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
class RadGroupCheckControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var radGroupCheckRepository: RadGroupCheckRepository

  @Test
  fun `should perform CRUD operations on RadGroupCheckEntity`() {
    val radGroupCheckJson = """
            {
                "groupName": "TestGroup",
                "attribute": "TestAttribute",
                "operator": "TestOperator",
                "value": "TestValue"
            }
        """.trimIndent()

    val createResponse = mockMvc.perform(
      post("/radgroupcheck")
        .contentType(MediaType.APPLICATION_JSON)
        .content(radGroupCheckJson)
    )
      .andExpect(status().isCreated)
      .andReturn()

    val location = createResponse.response.getHeader("Location")

    mockMvc.perform(get(location!!))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.groupName").value("TestGroup"))
      .andExpect(jsonPath("$.attribute").value("TestAttribute"))
      .andExpect(jsonPath("$.operator").value("TestOperator"))
      .andExpect(jsonPath("$.value").value("TestValue"))

    val updatedRadGroupCheckJson = """
            {
                "groupName": "UpdatedGroup",
                "attribute": "UpdatedAttribute",
                "operator": "UpdatedOperator",
                "value": "UpdatedValue"
            }
        """.trimIndent()

    mockMvc.perform(
      put(location)
        .contentType(MediaType.APPLICATION_JSON)
        .content(updatedRadGroupCheckJson)
    )
      .andExpect(status().isOk)

    mockMvc.perform(get(location))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.groupName").value("UpdatedGroup"))
      .andExpect(jsonPath("$.attribute").value("UpdatedAttribute"))
      .andExpect(jsonPath("$.operator").value("UpdatedOperator"))
      .andExpect(jsonPath("$.value").value("UpdatedValue"))

    mockMvc.perform(delete(location))
      .andExpect(status().isOk)

    mockMvc.perform(get(location))
      .andExpect(status().isNotFound)
  }

  @Test
  fun `should create a new RadGroupCheckEntity`() {
    val newRadGroupCheck = RadGroupCheckEntity(groupName = "Group1", attribute = "Attribute1", operator = "Operator1", value = "Value1")

    mockMvc.perform(
      post("/radgroupcheck")
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(newRadGroupCheck))
    )
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.groupName").value("Group1"))
  }

  @Test
  fun `should update an existing RadGroupCheckEntity`() {
    val existingRadGroupCheck = radGroupCheckRepository.save(RadGroupCheckEntity(groupName = "Group1", attribute = "Attribute1", operator = "Operator1", value = "Value1"))
    val updatedRadGroupCheck = existingRadGroupCheck.copy(value = "UpdatedValue")

    mockMvc.perform(
      put("/radgroupcheck/{id}", existingRadGroupCheck.id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(ObjectMapper().writeValueAsString(updatedRadGroupCheck))
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.value").value("UpdatedValue"))
  }

  @Test
  fun `should get a RadGroupCheckEntity by ID`() {
    val radGroupCheck = radGroupCheckRepository.save(RadGroupCheckEntity(groupName = "Group1", attribute = "Attribute1", operator = "Operator1", value = "Value1"))

    mockMvc.perform(
      get("/radgroupcheck/{id}", radGroupCheck.id)
        .accept(MediaType.APPLICATION_JSON)
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.groupName").value("Group1"))
  }

  @Test
  fun `should delete an existing RadGroupCheckEntity`() {
    val radGroupCheck = radGroupCheckRepository.save(RadGroupCheckEntity(groupName = "Group1", attribute = "Attribute1", operator = "Operator1", value = "Value1"))

    mockMvc.perform(
      delete("/radgroupcheck/{id}", radGroupCheck.id)
    )
      .andExpect(status().isOk)
  }



  @Test
  fun `should return bad request when creating RadGroupCheckEntity with missing groupName`() {
    val radGroupReply = RadGroupCheckEntity(
      attribute = "TestAttribute",
      operator = "TestOperator",
      value = "TestValue"
    )

    mockMvc.perform(
      post("/radgroupcheck")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].message").value("{empty.groupName}"))
  }

  @Test
  fun `should return bad request when creating RadGroupCheckEntity with missing attribute`() {
    val radGroupReply = RadGroupCheckEntity(
      groupName = "TestGroup",
      operator = "TestOperator",
      value = "TestValue"
    )

    mockMvc.perform(
      post("/radgroupcheck")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].message").value("{empty.attribute}"))
  }

  @Test
  fun `should return bad request when creating RadGroupCheckEntity with missing operator`() {
    val radGroupReply = RadGroupCheckEntity(
      groupName = "TestGroup",
      attribute = "TestAttribute",
      value = "TestValue"
    )

    mockMvc.perform(
      post("/radgroupcheck")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].message").value("{empty.operator}"))
  }

  @Test
  fun `should return bad request when creating RadGroupCheckEntity with missing value`() {
    val radGroupReply = RadGroupCheckEntity(
      groupName = "TestGroup",
      attribute = "TestAttribute",
      operator = "TestOperator"
    )

    mockMvc.perform(
      post("/radgroupcheck")
        .contentType("application/json")
        .content(ObjectMapper().writeValueAsString(radGroupReply))
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.errors[0].message").value("{empty.value}"))
  }
}
