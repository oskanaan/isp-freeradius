package com.thebinaryheap.ispradius.radius.radius

import com.thebinaryheap.ispradius.radius.repo.GroupEntity
import com.thebinaryheap.ispradius.radius.repo.GroupRepository
import com.thebinaryheap.ispradius.radius.repo.UniqueGroupNameValidator
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UniqueGroupNameValidatorTests {

  @Mock
  lateinit var groupRepository: GroupRepository

  @InjectMocks
  lateinit var uniqueGroupNameValidator: UniqueGroupNameValidator

  @BeforeEach
  fun setup() {
    MockitoAnnotations.openMocks(this)
  }

  @Test
  fun testIsValid() {
    val group = GroupEntity()
    group.id = 1
    group.groupName = "TestGroup"

    `when`(groupRepository.findByGroupName("TestGroup")).thenReturn(emptyList())
    
    val result = uniqueGroupNameValidator.isValid(group)
    assertTrue(result)
  }


  @Test
  fun testIsValid_UniqueName() {
    val group = GroupEntity()
    group.id = 1
    group.groupName = "UniqueName"

    `when`(groupRepository.findByGroupName("UniqueName")).thenReturn(emptyList())

    assertTrue(uniqueGroupNameValidator.isValid(group))
  }

  @Test
  fun testIsValid_NonUniqueName_DifferentId() {
    val existingGroup = GroupEntity()
    existingGroup.id = 2
    existingGroup.groupName = "NonUniqueName"

    val group = GroupEntity()
    group.id = 1
    group.groupName = "NonUniqueName"

    `when`(groupRepository.findByGroupName("NonUniqueName")).thenReturn(listOf(existingGroup))

    assertFalse(uniqueGroupNameValidator.isValid(group))
  }

  @Test
  fun testIsValid_NonUniqueName_SameId() {
    val existingGroup = GroupEntity()
    existingGroup.id = 1
    existingGroup.groupName = "NonUniqueName"

    val group = GroupEntity()
    group.id = 1
    group.groupName = "NonUniqueName"

    `when`(groupRepository.findByGroupName("NonUniqueName")).thenReturn(listOf(existingGroup))

    assertTrue(uniqueGroupNameValidator.isValid(group))
  }

  @Test
  fun testIsValid_EmptyGroupName() {
    val group = GroupEntity()
    group.id = 1
    group.groupName = ""

    assertTrue(uniqueGroupNameValidator.isValid(group))
  }

  @Test
  fun testIsValid_NullGroupName() {
    val group = GroupEntity()
    group.id = 1
    group.groupName = null

    assertTrue(uniqueGroupNameValidator.isValid(group))
  }
}

