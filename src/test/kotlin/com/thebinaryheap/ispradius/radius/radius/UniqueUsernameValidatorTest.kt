package com.thebinaryheap.ispradius.radius.radius

import com.thebinaryheap.ispradius.radius.repo.ClientEntity
import com.thebinaryheap.ispradius.radius.repo.ClientRepository
import com.thebinaryheap.ispradius.radius.repo.UniqueUsernameValidator
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class UniqueUsernameValidatorTest {

  private lateinit var validator: UniqueUsernameValidator

  @Mock
  private lateinit var clientRepository: ClientRepository

  @BeforeEach
  fun setUp() {
    MockitoAnnotations.initMocks(this)
    validator = UniqueUsernameValidator()
    validator.clientRepository = clientRepository
  }

  @Test
  fun `should return true when username is unique`() {
    val username = "uniqueuser"

    // Mock behavior to simulate a unique username
    Mockito.`when`(clientRepository.findByUsernameIgnoreCase(username)).thenReturn(emptyList())

    val isValid = validator.isValid(username)

    assertTrue(isValid)
  }

  @Test
  fun `should return false when username is not unique`() {
    val username = "existinguser"

    // Mock behavior to simulate a non-unique username
    Mockito.`when`(clientRepository.findByUsernameIgnoreCase(username)).thenReturn(listOf(ClientEntity()))

    val isValid = validator.isValid(username)

    assertFalse(isValid)
  }

  @Test
  fun `should return false when username is null`() {
    val username: String? = null

    val isValid = validator.isValid(username)

    assertFalse(isValid)
  }

  @Test
  fun `should return false when username is empty`() {
    val username = ""

    val isValid = validator.isValid(username)

    assertFalse(isValid)
  }
}
