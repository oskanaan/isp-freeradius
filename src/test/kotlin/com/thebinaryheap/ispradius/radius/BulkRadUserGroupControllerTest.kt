package com.thebinaryheap.ispradius.radius

import com.fasterxml.jackson.databind.ObjectMapper
import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.radius.repo.RadUserGroupEntity
import com.thebinaryheap.ispradius.radius.repo.RadUserGroupRepository
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(BulkRadUserGroupController::class)
class BulkRadUserGroupControllerTest {

    private val objectMapper: ObjectMapper = ObjectMapper()

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @MockBean
    private lateinit var radUserGroupRepository: RadUserGroupRepository

    @BeforeEach
    fun setUp() {
        Mockito.reset(radUserGroupRepository)
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        ApplicationContextProvider.applicationContext = webApplicationContext
    }

    @Test
    fun testCreateBulkRadUserGroups() {
        Mockito.`when`(radUserGroupRepository.saveAll(Mockito.anyList()))
            .thenReturn(
                mutableListOf(
                    RadUserGroupEntity(1, "user1", "group1", 1),
                    RadUserGroupEntity(2, "user2", "group2", 2)
                )
            )

        val userGroups = mutableListOf(
            RadUserGroupEntity(1, "user1", "group1", 1),
            RadUserGroupEntity(2, "user2", "group2", 2)
        )

        val requestBody = objectMapper.writeValueAsString(userGroups)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/radusergroups/bulk/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val content = result.response.contentAsString
        val savedUserGroups = objectMapper.readValue(content, List::class.java)

        assertEquals(2, savedUserGroups.size)
        assertEquals("user1", (savedUserGroups[0] as Map<String, Object>)["username"])
        assertEquals("user2", (savedUserGroups[1] as Map<String, Object>)["username"])
        assertEquals("group1", (savedUserGroups[0] as Map<String, Object>)["groupName"])
        assertEquals("group2", (savedUserGroups[1] as Map<String, Object>)["groupName"])
    }

    @Test
    fun testCreateBulkRadUserGroupsValidationFailure() {
        val userGroups = mutableListOf(
            RadUserGroupEntity() // Missing required fields
        )

        Mockito.`when`(radUserGroupRepository.saveAll(Mockito.anyList()))
            .thenThrow(ConstraintViolationException("Validation failed", emptySet()))

        val requestBody = objectMapper.writeValueAsString(userGroups)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/radusergroups/bulk/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun testDeleteBulkRadUserGroups() {
        // Create a list of RadUserGroupEntity objects to delete
        val userGroupsToDelete = mutableListOf(
            RadUserGroupEntity(1, "user1", "group1", 1),
            RadUserGroupEntity(2, "user2", "group2", 2)
        )

        Mockito.doNothing().`when`(radUserGroupRepository).deleteAll(userGroupsToDelete)

        val requestBody = objectMapper.writeValueAsString(userGroupsToDelete)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/radusergroups/bulk/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isNoContent)

        Mockito.verify(radUserGroupRepository, Mockito.times(1)).deleteAll(userGroupsToDelete)
    }

    @Test
    fun testDeleteBulkRadUserGroupsEmptyList() {
        val userGroupsToDelete = emptyList<RadUserGroupEntity>()

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/radusergroups/bulk/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userGroupsToDelete))
        )
            .andExpect(status().isNoContent)
    }
}
