package com.thebinaryheap.ispradius.radius.radius

import com.thebinaryheap.ispradius.radius.repo.RadcheckEntity
import com.thebinaryheap.ispradius.radius.repo.RadcheckRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class RadcheckRepositoryTest {

  @Autowired
  private lateinit var radcheckRepository: RadcheckRepository

  @Autowired
  private lateinit var entityManager: TestEntityManager

  @Test
  fun `should find RadcheckEntity by username`() {
    val radcheck1 = RadcheckEntity()
    radcheck1.username = "user1"
    radcheck1.attribute = "attribute1"
    radcheck1.operator = ":="
    radcheck1.value = "value1"

    val radcheck2 = RadcheckEntity()
    radcheck2.username = "user2"
    radcheck2.attribute = "attribute2"
    radcheck2.operator = ":="
    radcheck2.value = "value2"

    entityManager.persist(radcheck1)
    entityManager.persist(radcheck2)
    entityManager.flush()

    val result = radcheckRepository.findByUsernameIgnoreCase("user1", Pageable.ofSize(10))

    assertEquals(1, result.totalElements)
    assertEquals("user1", result.first().username)
    assertEquals("attribute1", result.first().attribute)
    assertEquals(":=", result.first().operator)
    assertEquals("value1", result.first().value)
  }

  @Test
  fun `should return an empty list when no RadcheckEntity with the given username exists`() {
    val result = radcheckRepository.findByUsernameIgnoreCase("nonexistentUser", Pageable.ofSize(10))
    assertEquals(0, result.totalElements)
  }
}
