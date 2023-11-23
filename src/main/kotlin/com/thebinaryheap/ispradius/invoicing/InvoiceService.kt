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
import com.thebinaryheap.ispradius.radius.repo.GroupRepository
import com.thebinaryheap.ispradius.radius.repo.RadUserGroupRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Service class responsible for business logic related to invoices.
 * It interacts with various repositories to manage invoice data and transactions.
 *
 * @property invoiceRepository Repository for invoice data access.
 * @property clientRepository Repository for client data access.
 * @property groupRepository Repository for group data access.
 * @property userGroupRepository Repository for user group data access.
 * @property invoiceTransactionRepository Repository for invoice transaction data access.
 * @property messageSource Source for localized messages.
 */
@Component
class InvoiceService(
  private val invoiceRepository: InvoiceRepository,
  private val clientRepository: ClientRepository,
  private val groupRepository: GroupRepository,
  private val userGroupRepository: RadUserGroupRepository,
  private val invoiceTransactionRepository: InvoiceTransactionRepository,
  private val messageSource: MessageSource
) {

  private var log: Logger = LoggerFactory.getLogger(InvoiceService::class.java)

  @Value("\${app.invoice.start-date}")
  lateinit var startDate: LocalDateTime

  @Scheduled(cron = "#{T(com.thebinaryheap.ispradius.settings.Settings).cronPattern}")
  fun generateInvoices() {
    if (LocalDateTime.now().isAfter(startDate)) {
      val clients = clientRepository.findByStatus(ClientStatus.ACTIVE)
      for (client in clients) {
        generateInvoiceForClient(client)
      }
    }
  }

  /**
   * Generates an invoice for a given client based on the current time and their subscription model.
   * Checks for existing invoices for the same period before creating a new one.
   *
   * @param client The client entity for whom the invoice will be generated.
   */
  fun generateInvoiceForClient(client: ClientEntity) {
    val now = LocalDateTime.now()
    val invoicePeriod: String = when (client.subscriptionModel) {
      ClientSubscriptionModel.MONTHLY -> "${now.month}-${now.year}"
      ClientSubscriptionModel.QUARTERLY -> "Q${(now.monthValue - 1) / 3 + 1}-${now.year}"
      ClientSubscriptionModel.YEARLY -> now.year.toString()
      else -> {
        log.error("Cannot generate invoice for client with ${client.id} since their subscription model is ${client.subscriptionModel} which is not a valid model.")
        return
      }
    }

    if (invoiceRepository.findByClientIdAndInvoicePeriod(client.id!!, invoicePeriod).isNotEmpty()) {
      log.debug("Invoice with client ID ${client.id} and period $invoicePeriod already exist, not generating.")
      return
    }

    val userGroups = userGroupRepository.findByUsername(client.username!!)
    var totalCost = userGroups.map {
      groupRepository.findByGroupName(it.groupName).first()
    }.sumOf { it.price ?: BigDecimal.ZERO }

    val invoices = invoiceRepository.findByClientIdAndStatusInOrderByGeneratedAtDesc(
      client.id!!,
      listOf(InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PENDING, InvoiceStatus.OVER_PAID)
    )

    var paidAmount = BigDecimal.ZERO
    var status = InvoiceStatus.PENDING
    var note = ""

    if (invoices.isNotEmpty()) {
      val lastInvoice = invoices.first()

      var carryOverAmount = BigDecimal.ZERO
      if (listOf(InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PENDING).contains(lastInvoice.status)) {
        carryOverAmount = lastInvoice.totalCost - lastInvoice.paidAmount
      }

      if (lastInvoice.status == InvoiceStatus.OVER_PAID) {
        paidAmount = lastInvoice.paidAmount - lastInvoice.totalCost
      }

      // Generate note if there's a carry-over amount
      note = if (carryOverAmount > BigDecimal.ZERO) {
        log.info("Found pending or partially paid invoices for client ${client.username}")
        totalCost += carryOverAmount
        messageSource.getMessage(
          "generate.invoice.carriedOverAmountNote",
          arrayOf(carryOverAmount),
          LocaleContextHolder.getLocale()
        )
      } else if (paidAmount > BigDecimal.ZERO) {
        messageSource.getMessage(
          "generate.invoice.carriedOverPaidAmountNote",
          arrayOf(paidAmount),
          LocaleContextHolder.getLocale()
        )
      } else ""

      if (paidAmount > BigDecimal.ZERO) {
        status = if (paidAmount > totalCost) {
          InvoiceStatus.OVER_PAID
        } else if (paidAmount < totalCost) {
          InvoiceStatus.PARTIALLY_PAID
        } else {
          InvoiceStatus.PAID
        }
      }

      if (listOf(
          InvoiceStatus.PARTIALLY_PAID,
          InvoiceStatus.OVER_PAID,
          InvoiceStatus.PENDING
        ).contains(lastInvoice.status)
      ) {
        lastInvoice.status = InvoiceStatus.CARRIED_OVER
        invoiceRepository.save(lastInvoice)
      }
    }

    // Create new invoice
    val invoice = InvoiceEntity(
      clientId = client.id,
      totalCost = totalCost,
      paidAmount = paidAmount,
      generatedAt = LocalDateTime.now(),
      note = note,
      status = status,
      invoicePeriod = invoicePeriod
    )

    invoiceRepository.save(invoice)
  }

  /**
   * Registers a payment against the most recent invoice of a client.
   * Updates the invoice's paid amount and status, and records the transaction.
   *
   * @param clientId The identifier of the client making the payment.
   * @param paidAmount The amount being paid.
   */
  fun payInvoice(clientId: Int, paidAmount: BigDecimal) {
    val invoice = invoiceRepository.findFirstByClientIdOrderByGeneratedAtDesc(clientId)

    if (invoice != null && paidAmount > BigDecimal.ZERO) {
      invoice.paidAmount = if (invoice.status != InvoiceStatus.PENDING) invoice.paidAmount + paidAmount else paidAmount //add to paid amount if partially paid
      invoice.status = if (invoice.paidAmount.compareTo(invoice.totalCost) == 0)
        InvoiceStatus.PAID
      else if (invoice.paidAmount.compareTo(invoice.totalCost) == -1)
        InvoiceStatus.PARTIALLY_PAID
      else
        InvoiceStatus.OVER_PAID

      invoiceRepository.save(invoice)

      val authentication = SecurityContextHolder.getContext().authentication
      val currentUsername = authentication.name
      val invoiceTransaction = InvoiceTransactionEntity(
        invoice = invoice,
        transactionAmount = paidAmount,
        invoiceTransactionType = InvoiceTransactionType.PAYMENT,
        invoiceStatus = invoice.status,
        description = messageSource.getMessage("payment.receivedBy", arrayOf(currentUsername), LocaleContextHolder.getLocale())
      )
      invoiceTransactionRepository.save(invoiceTransaction)
    }
  }

  /**
   * Adjusts the total cost of an invoice by a specified amount.
   * Reflects the adjustment in the invoice status and records the transaction.
   *
   * @param invoiceId The identifier of the invoice to be adjusted.
   * @param amount The amount to adjust the invoice by.
   */
  fun adjustAmount(invoiceId: Long, amount: BigDecimal) {
    val invoice = invoiceRepository.findById(invoiceId).orElse(null)

    if (invoice != null) {
      invoice.totalCost += amount
      invoice.status = if (invoice.paidAmount.compareTo(invoice.totalCost) == 0)
        InvoiceStatus.PAID
      else if (invoice.paidAmount.compareTo(invoice.totalCost) == -1)
        InvoiceStatus.PARTIALLY_PAID
      else
        InvoiceStatus.OVER_PAID

      invoiceRepository.save(invoice)

      val authentication = SecurityContextHolder.getContext().authentication
      val currentUsername = authentication.name
      val invoiceTransaction = InvoiceTransactionEntity(
        invoice = invoice,
        transactionAmount = amount,
        invoiceTransactionType = InvoiceTransactionType.PAYMENT,
        invoiceStatus = invoice.status,
        description = messageSource.getMessage("adjustment.doneBy", arrayOf(currentUsername), LocaleContextHolder.getLocale())
      )
      invoiceTransactionRepository.save(invoiceTransaction)
    }
  }
}
