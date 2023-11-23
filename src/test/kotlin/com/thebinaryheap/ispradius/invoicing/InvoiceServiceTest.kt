package com.thebinaryheap.ispradius.invoicing

import com.thebinaryheap.ispradius.invoicing.repo.InvoiceEntity
import com.thebinaryheap.ispradius.invoicing.repo.InvoiceRepository
import com.thebinaryheap.ispradius.invoicing.repo.InvoiceStatus
import com.thebinaryheap.ispradius.invoicing.repo.InvoiceTransactionEntity
import com.thebinaryheap.ispradius.invoicing.repo.InvoiceTransactionRepository
import com.thebinaryheap.ispradius.invoicing.repo.InvoiceTransactionType
import com.thebinaryheap.ispradius.radius.repo.ClientEntity
import com.thebinaryheap.ispradius.radius.repo.ClientRepository
import com.thebinaryheap.ispradius.radius.repo.ClientStatus
import com.thebinaryheap.ispradius.radius.repo.ClientSubscriptionModel
import com.thebinaryheap.ispradius.radius.repo.GroupEntity
import com.thebinaryheap.ispradius.radius.repo.GroupRepository
import com.thebinaryheap.ispradius.radius.repo.RadUserGroupEntity
import com.thebinaryheap.ispradius.radius.repo.RadUserGroupRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

@ExtendWith(MockitoExtension::class)
class InvoiceServiceTest {

  @Mock
  private lateinit var invoiceRepository: InvoiceRepository

  @Mock
  private lateinit var invoiceTransactionRepository: InvoiceTransactionRepository

  @Mock
  private lateinit var clientRepository: ClientRepository

  @Mock
  private lateinit var groupRepository: GroupRepository

  @Mock
  private lateinit var userGroupRepository: RadUserGroupRepository

  @Mock
  private lateinit var messageSource: MessageSource

  @Captor
  lateinit var invoiceTransactionCaptor: ArgumentCaptor<InvoiceTransactionEntity>

  @Captor
  lateinit var invoiceCaptor: ArgumentCaptor<InvoiceEntity>

  @InjectMocks
  private lateinit var invoiceService: InvoiceService

  @BeforeEach
  fun before() {
    val auth = UsernamePasswordAuthenticationToken(
      User("testUser", "password", listOf(SimpleGrantedAuthority("ROLE_USER"))),
      null,
      listOf(SimpleGrantedAuthority("ROLE_USER"))
    )
    SecurityContextHolder.getContext().authentication = auth

    Mockito.reset(messageSource)
  }

  @Test
  fun `test generateInvoices with carry over`() {
    val client = ClientEntity(id = 1, username = "testUser", subscriptionModel = ClientSubscriptionModel.MONTHLY)
    val group = GroupEntity(groupName = "testGroup", price = BigDecimal(100))
    val userGroup = RadUserGroupEntity(username = "testUser", groupName = "testGroup")
    val lastInvoice = InvoiceEntity(id = 1L, client = client, totalCost = BigDecimal(100), paidAmount = BigDecimal(50), status = InvoiceStatus.PARTIALLY_PAID)

    `when`(clientRepository.findByStatus(ClientStatus.ACTIVE)).thenReturn(listOf(client))
    `when`(userGroupRepository.findByUsername(anyString())).thenReturn(listOf(userGroup))
    `when`(groupRepository.findByGroupName(anyString())).thenReturn(listOf(group))
    `when`(invoiceRepository.findByClientIdAndStatusInOrderByGeneratedAtDesc(client.id!!, listOf(InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PENDING, InvoiceStatus.OVER_PAID))).thenReturn(listOf(lastInvoice))
    `when`(messageSource.getMessage(any(), any(), any())).thenReturn("Some message")

    invoiceService.startDate = LocalDateTime.now()
    invoiceService.generateInvoices()

    val invoiceCaptor = ArgumentCaptor.forClass(InvoiceEntity::class.java)
    verify(invoiceRepository, times(2)).save(invoiceCaptor.capture())
    val capturedInvoice = invoiceCaptor.allValues

    assertEquals(InvoiceStatus.CARRIED_OVER, capturedInvoice[0].status)
    assertEquals(BigDecimal(150), capturedInvoice[1].totalCost)
    assertEquals(BigDecimal(0), capturedInvoice[1].paidAmount)
    assertEquals(InvoiceStatus.PENDING, capturedInvoice[1].status)
    assertEquals("Some message", capturedInvoice[1].note)
  }

  @Test
  fun `test generateInvoices with overpaid`() {
    val client = ClientEntity(id = 1, username = "testUser", subscriptionModel = ClientSubscriptionModel.MONTHLY)
    val group = GroupEntity(groupName = "testGroup", price = BigDecimal(100))
    val userGroup = RadUserGroupEntity(username = "testUser", groupName = "testGroup")
    val lastInvoice = InvoiceEntity(id = 1L, client = client, totalCost = BigDecimal(100), paidAmount = BigDecimal(150), status = InvoiceStatus.OVER_PAID)

    `when`(clientRepository.findByStatus(ClientStatus.ACTIVE)).thenReturn(listOf(client))
    `when`(userGroupRepository.findByUsername(anyString())).thenReturn(listOf(userGroup))
    `when`(groupRepository.findByGroupName(anyString())).thenReturn(listOf(group))
    `when`(invoiceRepository.findByClientIdAndStatusInOrderByGeneratedAtDesc(client.id!!, listOf(InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PENDING, InvoiceStatus.OVER_PAID))).thenReturn(listOf(lastInvoice))
    `when`(messageSource.getMessage(any(), any(), any())).thenReturn("Some message")

    invoiceService.startDate = LocalDateTime.now()
    invoiceService.generateInvoices()

    val invoiceCaptor = ArgumentCaptor.forClass(InvoiceEntity::class.java)
    verify(invoiceRepository, times(2)).save(invoiceCaptor.capture())
    val capturedInvoice = invoiceCaptor.allValues

    assertEquals(BigDecimal(100), capturedInvoice[1].totalCost)
    assertEquals(BigDecimal(50), capturedInvoice[1].paidAmount)
    assertEquals(InvoiceStatus.PARTIALLY_PAID, capturedInvoice[1].status)
    assertEquals(InvoiceStatus.CARRIED_OVER, capturedInvoice[0].status)
    assertEquals("Some message", capturedInvoice[1].note)
  }

  @Test
  fun `test generateInvoices with overpaid invoice that exactly covers next month`() {
    val client = ClientEntity(id = 1, username = "testUser", subscriptionModel = ClientSubscriptionModel.MONTHLY)
    val group = GroupEntity(groupName = "testGroup", price = BigDecimal(100))
    val userGroup = RadUserGroupEntity(username = "testUser", groupName = "testGroup")
    val lastInvoice = InvoiceEntity(id = 1L, client = client, totalCost = BigDecimal(100), paidAmount = BigDecimal(200), status = InvoiceStatus.OVER_PAID)

    `when`(clientRepository.findByStatus(ClientStatus.ACTIVE)).thenReturn(listOf(client))
    `when`(userGroupRepository.findByUsername(anyString())).thenReturn(listOf(userGroup))
    `when`(groupRepository.findByGroupName(anyString())).thenReturn(listOf(group))
    `when`(invoiceRepository.findByClientIdAndStatusInOrderByGeneratedAtDesc(client.id!!, listOf(InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PENDING, InvoiceStatus.OVER_PAID))).thenReturn(listOf(lastInvoice))
    `when`(messageSource.getMessage(any(), any(), any())).thenReturn("Some message")

    invoiceService.startDate = LocalDateTime.now()
    invoiceService.generateInvoices()

    val invoiceCaptor = ArgumentCaptor.forClass(InvoiceEntity::class.java)
    verify(invoiceRepository, times(2)).save(invoiceCaptor.capture())
    val capturedInvoice = invoiceCaptor.allValues

    assertEquals(BigDecimal(100), capturedInvoice[1].totalCost)
    assertEquals(BigDecimal(100), capturedInvoice[1].paidAmount)
    assertEquals(InvoiceStatus.PAID, capturedInvoice[1].status)
    assertEquals(InvoiceStatus.CARRIED_OVER, capturedInvoice[0].status)
    assertEquals("Some message", capturedInvoice[1].note)
  }

  @Test
  fun `test generateInvoices with overpaid invoice that over pays next month`() {
    val client = ClientEntity(id = 1, username = "testUser", subscriptionModel = ClientSubscriptionModel.MONTHLY)
    val group = GroupEntity(groupName = "testGroup", price = BigDecimal(100))
    val userGroup = RadUserGroupEntity(username = "testUser", groupName = "testGroup")
    val lastInvoice = InvoiceEntity(id = 1L, client = client, totalCost = BigDecimal(100), paidAmount = BigDecimal(220), status = InvoiceStatus.OVER_PAID)

    `when`(clientRepository.findByStatus(ClientStatus.ACTIVE)).thenReturn(listOf(client))
    `when`(userGroupRepository.findByUsername(anyString())).thenReturn(listOf(userGroup))
    `when`(groupRepository.findByGroupName(anyString())).thenReturn(listOf(group))
    `when`(invoiceRepository.findByClientIdAndStatusInOrderByGeneratedAtDesc(client.id!!, listOf(InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PENDING, InvoiceStatus.OVER_PAID))).thenReturn(listOf(lastInvoice))
    `when`(messageSource.getMessage(any(), any(), any())).thenReturn("Some message")

    invoiceService.startDate = LocalDateTime.now()
    invoiceService.generateInvoices()

    val invoiceCaptor = ArgumentCaptor.forClass(InvoiceEntity::class.java)
    verify(invoiceRepository, times(2)).save(invoiceCaptor.capture())
    val capturedInvoice = invoiceCaptor.allValues

    assertEquals(BigDecimal(100), capturedInvoice[1].totalCost)
    assertEquals(BigDecimal(120), capturedInvoice[1].paidAmount)
    assertEquals(InvoiceStatus.OVER_PAID, capturedInvoice[1].status)
    assertEquals(InvoiceStatus.CARRIED_OVER, capturedInvoice[0].status)
    assertEquals("Some message", capturedInvoice[1].note)
  }

  @Test
  fun `test payInvoice partially paid`() {
    val invoice = InvoiceEntity(id = 1L, totalCost = BigDecimal(100), client = ClientEntity(1), paidAmount = BigDecimal.ZERO)
    `when`(invoiceRepository.findFirstByClientIdOrderByGeneratedAtDesc(anyInt())).thenReturn(invoice)

    `when`(messageSource.getMessage(any(), any(), any())).thenReturn("Some message")
    invoiceService.payInvoice(1, BigDecimal(50))

    val invoiceCaptor = ArgumentCaptor.forClass(InvoiceEntity::class.java)
    verify(invoiceRepository).save(invoiceCaptor.capture())
    val capturedInvoice = invoiceCaptor.value

    assertEquals(BigDecimal(50), capturedInvoice.paidAmount)
    assertEquals(InvoiceStatus.PARTIALLY_PAID, capturedInvoice.status)
  }

  @Test
  fun `test payInvoice fully paid`() {
    `when`(messageSource.getMessage(any(), any(), any())).thenReturn("Some message")
    val invoice = InvoiceEntity(id = 1L, totalCost = BigDecimal(100), client = ClientEntity(1), paidAmount = BigDecimal.ZERO)
    `when`(invoiceRepository.findFirstByClientIdOrderByGeneratedAtDesc(anyInt())).thenReturn(invoice)

    invoiceService.payInvoice(1, BigDecimal(100))

    val invoiceCaptor = ArgumentCaptor.forClass(InvoiceEntity::class.java)
    verify(invoiceRepository).save(invoiceCaptor.capture())
    val capturedInvoice = invoiceCaptor.value

    assertEquals(BigDecimal(100), capturedInvoice.paidAmount)
    assertEquals(InvoiceStatus.PAID, capturedInvoice.status)
  }

  @Test
  fun `test payInvoice fully paid for a partial paid invoice`() {
    val invoice = InvoiceEntity(id = 1L, totalCost = BigDecimal(100), client = ClientEntity(1), paidAmount = BigDecimal(50), status = InvoiceStatus.PARTIALLY_PAID)
    `when`(invoiceRepository.findFirstByClientIdOrderByGeneratedAtDesc(anyInt())).thenReturn(invoice)

    `when`(messageSource.getMessage(any(), any(), any())).thenReturn("Some message")
    invoiceService.payInvoice(1, BigDecimal(50))

    val invoiceCaptor = ArgumentCaptor.forClass(InvoiceEntity::class.java)
    verify(invoiceRepository).save(invoiceCaptor.capture())
    val capturedInvoice = invoiceCaptor.value

    assertEquals(BigDecimal(100), capturedInvoice.paidAmount)
    assertEquals(InvoiceStatus.PAID, capturedInvoice.status)
  }


  @Test
  fun `test generateInvoices without carry over`() {
    val client = ClientEntity(id = 1, username = "testUser", subscriptionModel = ClientSubscriptionModel.MONTHLY)
    val group = GroupEntity(groupName = "testGroup", price = BigDecimal(100))
    val userGroup = RadUserGroupEntity(username = "testUser", groupName = "testGroup")

    `when`(clientRepository.findByStatus(ClientStatus.ACTIVE)).thenReturn(listOf(client))
    `when`(userGroupRepository.findByUsername(anyString())).thenReturn(listOf(userGroup))
    `when`(groupRepository.findByGroupName(anyString())).thenReturn(listOf(group))

    invoiceService.startDate = LocalDateTime.now()
    invoiceService.generateInvoices()

    val invoiceCaptor = ArgumentCaptor.forClass(InvoiceEntity::class.java)
    verify(invoiceRepository).save(invoiceCaptor.capture())
    val capturedInvoice = invoiceCaptor.value

    assertEquals(BigDecimal(100), capturedInvoice.totalCost)
    assertEquals("", capturedInvoice.note)
  }

  @Test
  fun `test payInvoice invalid invoiceId`() {
    `when`(invoiceRepository.findFirstByClientIdOrderByGeneratedAtDesc(anyInt())).thenReturn(null)

    invoiceService.payInvoice(999, BigDecimal(50))

    verify(invoiceRepository, never()).save(any())
  }

  @Test
  fun `test payInvoice zero amount`() {
    val invoice = InvoiceEntity(id = 1L, totalCost = BigDecimal(100), client = ClientEntity(id = 1, username = "testUser"), paidAmount = BigDecimal.ZERO)
    `when`(invoiceRepository.findFirstByClientIdOrderByGeneratedAtDesc(anyInt())).thenReturn(invoice)

    invoiceService.payInvoice(1, BigDecimal.ZERO)

    verify(invoiceRepository, never()).save(any())
  }

  @Test
  fun `should adjust invoice amount`() {
    val invoiceId = 1L
    val originalAmount = BigDecimal("100.0")
    val adjustAmount = BigDecimal("10.0")

    val invoice = InvoiceEntity(
      id = invoiceId,
      clientId = 1,
      totalCost = originalAmount,
      paidAmount = BigDecimal.ZERO,
      generatedAt = LocalDateTime.now(),
      note = "",
      status = InvoiceStatus.PENDING
    )

    // mock repository and message source behavior
    `when`(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice))
    `when`(messageSource.getMessage("adjustment.doneBy", arrayOf("testUser"), LocaleContextHolder.getLocale())).thenReturn("Adjusted by testUser")

    // call the method to test
    invoiceService.adjustAmount(invoiceId, adjustAmount)

    // assert the invoice amount adjustment
    assert(invoice.totalCost == originalAmount + adjustAmount)
  }

  @Test
  fun `should adjust invoice amount and create InvoiceTransactionEntity`() {
    val invoiceId = 1L
    val originalAmount = BigDecimal("100.0")
    val adjustAmount = BigDecimal("10.0")

    val invoice = InvoiceEntity(
      id = invoiceId,
      clientId = 1,
      totalCost = originalAmount,
      paidAmount = BigDecimal.ZERO,
      generatedAt = LocalDateTime.now(),
      note = "",
      status = InvoiceStatus.PENDING
    )

    // Mock repository and message source behavior
    `when`(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice))
    `when`(messageSource.getMessage(anyString(), any(), any())).thenReturn("Adjusted by testUser")

    // Call the method to test
    invoiceService.adjustAmount(invoiceId, adjustAmount)

    // Capture the InvoiceTransactionEntity saved in the repository
    verify(invoiceTransactionRepository).save(invoiceTransactionCaptor.capture())

    // Assert the invoice amount adjustment
    assertEquals(originalAmount + adjustAmount, invoice.totalCost)

    // Assert the InvoiceTransactionEntity fields
    val invoiceTransaction = invoiceTransactionCaptor.value
    assertNotNull(invoiceTransaction)
    assertEquals(InvoiceTransactionType.PAYMENT, invoiceTransaction.invoiceTransactionType)
    assertEquals(invoice.status, invoiceTransaction.invoiceStatus)
    assertEquals(adjustAmount, invoiceTransaction.transactionAmount)
  }

  @Test
  fun `should not adjust invoice amount when invoice not found`() {
    val invoiceId = 1L
    val adjustAmount = BigDecimal("10.0")

    // Mock repository to return empty
    `when`(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty())

    // Call the method to test
    invoiceService.adjustAmount(invoiceId, adjustAmount)

    // Verify that no InvoiceTransactionEntity is saved
    verify(invoiceTransactionRepository, never()).save(any())
  }

  @Test
  fun `should correctly calculate invoice amount`() {
    // Arrange
    val clientId = 1
    val client = ClientEntity(id = clientId, status = ClientStatus.ACTIVE, username = "username", subscriptionModel = ClientSubscriptionModel.MONTHLY)
    val userGroup1 = RadUserGroupEntity(username = "username", groupName =  "group1")
    val userGroup2 = RadUserGroupEntity(username = "username", groupName = "group2")
    val group1 = GroupEntity(groupName = "group1", price = BigDecimal("10.00"))
    val group2 = GroupEntity(groupName = "group2", price =BigDecimal("15.00"))

    whenever(userGroupRepository.findByUsername(client.username!!)).thenReturn(listOf(userGroup1, userGroup2))
    whenever(groupRepository.findByGroupName("group1")).thenReturn(listOf(group1))
    whenever(groupRepository.findByGroupName("group2")).thenReturn(listOf(group2))

    // Act
    val invoice = invoiceService.generateInvoiceForClient(client)
    verify(invoiceRepository).save(invoiceCaptor.capture())

    // Assert
    assertEquals(BigDecimal("25.00"), invoiceCaptor.value.totalCost) // 10 + 15
  }

  @Test
  fun `should correctly transition invoice status`() {
    // Arrange
    val clientId = 1
    val initialInvoice = InvoiceEntity(
      clientId = clientId,
      totalCost = BigDecimal("100.00"),
      paidAmount = BigDecimal.ZERO,
      generatedAt = LocalDateTime.now(),
      note = "",
      status = InvoiceStatus.PENDING
    )
    `when`(messageSource.getMessage(any(), any(), any())).thenReturn("Some message")

    whenever(invoiceRepository.findFirstByClientIdOrderByGeneratedAtDesc(clientId)).thenReturn(initialInvoice)

    // Act & Assert for Partial Payment
    invoiceService.payInvoice(clientId, BigDecimal("50.00"))
    assertEquals(InvoiceStatus.PARTIALLY_PAID, initialInvoice.status)

    // Act & Assert for Full Payment
    invoiceService.payInvoice(clientId, BigDecimal("50.00"))
    assertEquals(InvoiceStatus.PAID, initialInvoice.status)
  }

  @Test
  fun `should not generate duplicate invoices`() {
    // Arrange
    val clientId = 1
    val client = ClientEntity(id = clientId, status = ClientStatus.ACTIVE, username = "username", subscriptionModel = ClientSubscriptionModel.MONTHLY)
    val existingInvoice = InvoiceEntity(
      clientId = clientId,
      totalCost = BigDecimal("100.00"),
      paidAmount = BigDecimal.ZERO,
      generatedAt = LocalDateTime.now(),
      note = "",
      status = InvoiceStatus.PENDING,
      invoicePeriod = LocalDateTime.now().month.toString()
    )
    whenever(invoiceRepository.findByClientIdAndInvoicePeriod(client.id!!, "${LocalDateTime.now().month}-${LocalDateTime.now().year}")).thenReturn(listOf(existingInvoice))
    // Act
    invoiceService.generateInvoiceForClient(client)

    // Assert: Verify that a new invoice is not saved
    verify(invoiceRepository, times(0)).save(any(InvoiceEntity::class.java))
  }

  @Test
  fun `should correctly set invoicePeriod based on subscription model`() {
    // Arrange
    val monthlyClient = ClientEntity(id = 1, username = "monthlyUser", subscriptionModel = ClientSubscriptionModel.MONTHLY)
    val quarterlyClient = ClientEntity(id = 2, username = "quarterlyUser", subscriptionModel = ClientSubscriptionModel.QUARTERLY)
    val yearlyClient = ClientEntity(id = 3, username = "yearlyUser", subscriptionModel = ClientSubscriptionModel.YEARLY)

    val now = LocalDateTime.now()
    val expectedMonthlyPeriod = "${now.month}-${now.year}"
    val expectedQuarterlyPeriod = "Q${(now.monthValue - 1) / 3 + 1}-${now.year}"
    val expectedYearlyPeriod = now.year.toString()

    // Act
    invoiceService.generateInvoiceForClient(monthlyClient)
    invoiceService.generateInvoiceForClient(quarterlyClient)
    invoiceService.generateInvoiceForClient(yearlyClient)

    // Capture the saved invoices
    verify(invoiceRepository, times(3)).save(invoiceCaptor.capture())
    val capturedInvoices = invoiceCaptor.allValues

    // Assert
    assertEquals(expectedMonthlyPeriod, capturedInvoices[0].invoicePeriod)
    assertEquals(expectedQuarterlyPeriod, capturedInvoices[1].invoicePeriod)
    assertEquals(expectedYearlyPeriod, capturedInvoices[2].invoicePeriod)
  }

}
