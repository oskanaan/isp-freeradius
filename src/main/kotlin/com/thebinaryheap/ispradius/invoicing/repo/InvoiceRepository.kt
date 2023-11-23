package com.thebinaryheap.ispradius.invoicing.repo

import com.thebinaryheap.ispradius.radius.repo.ClientEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.transaction.annotation.Transactional

/**
 * Repository interface for invoice entities, providing CRUD operations and custom queries for working with invoices.
 * It extends `PagingAndSortingRepository` to support pagination and sorting, and `CrudRepository` for basic CRUD operations.
 */
@RepositoryRestResource(collectionResourceRel = "invoices", path = "invoices")
interface InvoiceRepository : PagingAndSortingRepository<InvoiceEntity, Long>, CrudRepository<InvoiceEntity, Long> {
  @Transactional(readOnly = true)
  fun findByClientIdOrderByGeneratedAtDesc(clientId: Int, pageable: Pageable): Page<InvoiceEntity>

  @Transactional(readOnly = true)
  fun findFirstByClientIdOrderByGeneratedAtDesc(clientId: Int): InvoiceEntity?

  @Transactional(readOnly = true)
  fun findByClientIdAndStatusInOrderByGeneratedAtDesc(clientId: Int, statuses: List<InvoiceStatus>): List<InvoiceEntity>

  @Transactional(readOnly = true)
  fun findByClientIdAndInvoicePeriod(clientId: Int, invoicePeriod: String): List<InvoiceEntity>

  @Transactional(readOnly = true)
  @Query("SELECT e FROM InvoiceEntity e left join fetch e.client WHERE " +
      " (:status IS NULL OR e.status = :status) " +
      "AND (:username IS NULL OR :username = '' OR LOWER(e.client.username) = LOWER(:username)) " +
      "AND (:phone IS NULL OR :phone = '' OR LOWER(e.client.phoneNumber) = LOWER(:phone)) " +
      "ORDER BY e.generatedAt desc")
  fun searchInvoices(@Param("username") username: String?,
                     @Param("phone") phone: String?,
                     @Param("status") status: InvoiceStatus?,
                     pageable: Pageable): Page<InvoiceEntity>

  @Transactional(readOnly = true)
  @EntityGraph(attributePaths = ["client"])
  override fun findById(@Param("id") id: Long): Optional<InvoiceEntity>
}

@RepositoryRestResource(collectionResourceRel = "transactions", path = "transactions")
interface InvoiceTransactionRepository : PagingAndSortingRepository<InvoiceTransactionEntity, Long>, CrudRepository<InvoiceTransactionEntity, Long> {

}

/**
 * Represents an invoice entity mapped to the "invoices" table in the database.
 * It includes information about the client, total cost, status, and associated transactions.
 *
 * @property id The unique identifier for the invoice.
 * @property client The client associated with this invoice.
 * @property clientId The foreign key to the client entity.
 * @property totalCost The total cost of the invoice.
 * @property paidAmount The amount that has been paid towards the invoice.
 * @property generatedAt The timestamp when the invoice was generated.
 * @property note Any additional notes regarding the invoice.
 * @property status The current status of the invoice.
 * @property invoicePeriod The period that the invoice covers.
 * @property transactions A list of transactions associated with this invoice.
 */
@Entity
@Table(name = "invoices")
data class InvoiceEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "client_id", insertable = false, updatable = false)
  @RestResource(path = "clients", rel="client")
  var client: ClientEntity? = null,

  @Column(name = "client_id", nullable = false)
  var clientId: Int? = null,

  @Column(precision = 10, scale = 2)
  var totalCost: BigDecimal,

  @Column(precision = 10, scale = 2)
  var paidAmount: BigDecimal,

  @Column
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  var generatedAt: LocalDateTime = LocalDateTime.now(),

  @Column
  var note: String = "",

  @Enumerated(EnumType.STRING)
  @Column
  var status: InvoiceStatus = InvoiceStatus.PENDING,

  @Column
  var invoicePeriod: String? = null,

  @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
  var transactions: List<InvoiceTransactionEntity> = mutableListOf()

)

@Entity
@Table(name = "invoice_transactions")
data class InvoiceTransactionEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id")
  var invoice: InvoiceEntity,

  @Column(precision = 10, scale = 2)
  var transactionAmount: BigDecimal,

  @Enumerated(EnumType.STRING)
  @Column
  var invoiceTransactionType: InvoiceTransactionType,

  @Enumerated(EnumType.STRING)
  @Column
  var invoiceStatus: InvoiceStatus,

  @Column
  var transactionAt: LocalDateTime = LocalDateTime.now(),

  @Column
  var description: String = ""
)

enum class InvoiceStatus {
  PENDING,
  PARTIALLY_PAID,
  PAID,
  OVER_PAID,
  CANCELLED,
  CARRIED_OVER
}

enum class InvoiceTransactionType {
  PAYMENT,
  ADJUSTMENT
}