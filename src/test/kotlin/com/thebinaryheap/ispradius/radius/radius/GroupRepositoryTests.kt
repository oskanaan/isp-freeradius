package com.thebinaryheap.ispradius.radius.radius

import com.thebinaryheap.ispradius.radius.repo.GroupEntity
import com.thebinaryheap.ispradius.radius.repo.GroupRepository
import com.thebinaryheap.ispradius.radius.repo.UniqueGroupNameValidator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class GroupRepositoryTests {

  @Mock
  lateinit var groupRepository: GroupRepository

  @Mock
  lateinit var pageable: Pageable

  @Test
  fun testFindByGroupNameIgnoreCase() {
    val group = GroupEntity()
    group.id = 1
    group.groupName = "TestGroup"
    val pageable: Pageable = mock(Pageable::class.java)
    val groups = listOf(group)
    val pagedResult = PageImpl(groups)
    
    `when`(groupRepository.findByGroupNameIgnoreCase("TestGroup", pageable)).thenReturn(pagedResult)

    val result = groupRepository.findByGroupNameIgnoreCase("TestGroup", pageable)
    assertNotNull(result)
    assertEquals(1, result.totalElements)
  }

  @Test
  fun testFindByGroupNameIgnoreCase_NoResult() {
    `when`(groupRepository.findByGroupNameIgnoreCase("TestGroup", pageable)).thenReturn(PageImpl(emptyList()))
    val result = groupRepository.findByGroupNameIgnoreCase("TestGroup", pageable)
    assertNotNull(result)
    assertEquals(0, result.totalElements)
  }

  @Test
  fun testFindByGroupNameIgnoreCase_MultipleResults() {
    val group1 = GroupEntity()
    group1.id = 1
    group1.groupName = "TestGroup"

    val group2 = GroupEntity()
    group2.id = 2
    group2.groupName = "TestGroup"

    val groups = listOf(group1, group2)
    `when`(groupRepository.findByGroupNameIgnoreCase("TestGroup", pageable)).thenReturn(PageImpl(groups))

    val result = groupRepository.findByGroupNameIgnoreCase("TestGroup", pageable)
    assertNotNull(result)
    assertEquals(2, result.totalElements)
  }


  @Test
  fun testFindByGroupNameIgnoreCase_CaseInsensitive() {
    val group = GroupEntity()
    group.id = 1
    group.groupName = "TestGroup"
    `when`(groupRepository.findByGroupNameIgnoreCase("testgroup", pageable)).thenReturn(PageImpl(listOf(group)))

    val result = groupRepository.findByGroupNameIgnoreCase("testgroup", pageable)
    assertNotNull(result)
    assertEquals(1, result.totalElements)
  }

  @Test
  fun testFindByGroupNameIgnoreCase_EmptyQuery() {
    `when`(groupRepository.findByGroupNameIgnoreCase("", pageable)).thenReturn(PageImpl(emptyList()))
    val result = groupRepository.findByGroupNameIgnoreCase("", pageable)
    assertNotNull(result)
    assertEquals(0, result.totalElements)
  }

  @Test
  fun testFindByGroupName_FlushModeCommit() {
    val group = GroupEntity()
    group.id = 1
    group.groupName = "TestGroup"
    `when`(groupRepository.findByGroupName("TestGroup")).thenReturn(listOf(group))

    val result = groupRepository.findByGroupName("TestGroup")
    assertNotNull(result)
    assertEquals(1, result.size)
  }
}
