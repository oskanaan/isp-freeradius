package com.thebinaryheap.ispradius.radius.radius

import com.thebinaryheap.ispradius.TestRestConfiguration
import com.thebinaryheap.ispradius.radius.repo.RadUserGroupEntity
import com.thebinaryheap.ispradius.radius.repo.RadUserGroupRepository
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [TestRestConfiguration::class])
class RadUserGroupRepositoryTest {

  @Autowired
  private lateinit var radUserGroupRepository: RadUserGroupRepository

  @BeforeEach
  fun setup() {
    radUserGroupRepository.deleteAll()
  }

  @Test
  fun `should find RadUserGroups by username`() {
    val username = "user1"
    val radUserGroup1 = radUserGroupRepository.save(RadUserGroupEntity(username = username, groupName = "group1", priority = 1))
    val radUserGroup2 = radUserGroupRepository.save(RadUserGroupEntity(username = username, groupName = "group2", priority = 2))

    val foundUserGroups = radUserGroupRepository.findByUsername(username)

    assertThat(foundUserGroups).hasSize(2)
    assertThat(foundUserGroups).contains(radUserGroup1, radUserGroup2)
  }

  @Test
  fun `should save a new RadUserGroup`() {
    val radUserGroup = RadUserGroupEntity(username = "user2", groupName = "group3", priority = 3)

    val savedUserGroup = radUserGroupRepository.save(radUserGroup)

    assertThat(savedUserGroup.id).isNotNull()
    assertThat(radUserGroupRepository.findById(savedUserGroup.id!!)).isPresent()
  }

  @Test
  fun `should update an existing RadUserGroup`() {
    val radUserGroup = RadUserGroupEntity(username = "user3", groupName = "group4", priority = 4)
    val savedUserGroup = radUserGroupRepository.save(radUserGroup)

    savedUserGroup.groupName = "updatedGroup"
    val updatedUserGroup = radUserGroupRepository.save(savedUserGroup)

    assertThat(updatedUserGroup.groupName).isEqualTo("updatedGroup")
  }

  @Test
  fun `should delete an existing RadUserGroup`() {
    val radUserGroup = RadUserGroupEntity(username = "user4", groupName = "group5", priority = 5)
    val savedUserGroup = radUserGroupRepository.save(radUserGroup)

    radUserGroupRepository.deleteById(savedUserGroup.id!!)

    assertThat(radUserGroupRepository.findById(savedUserGroup.id!!)).isEmpty()
  }

  @Test
  fun `should return an empty list for a non-existent username`() {
    val username = "nonexistentuser"

    val foundUserGroups = radUserGroupRepository.findByUsername(username)

    assertThat(foundUserGroups).isEmpty()
  }

  @Test
  fun `should not save a RadUserGroup with missing required fields`() {
    val radUserGroup = RadUserGroupEntity() // Missing required fields

    assertThrows<ConstraintViolationException> {
      radUserGroupRepository.save(radUserGroup)
    }
  }
}
