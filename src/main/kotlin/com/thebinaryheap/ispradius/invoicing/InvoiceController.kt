package com.thebinaryheap.ispradius.invoicing

import java.math.BigDecimal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for handling invoice-related actions such as paying an invoice, adjusting invoice amounts,
 * and generating invoices immediately.
 */
@RestController
@RequestMapping("/invoices")
class InvoiceController {

  /**
   * Service layer for invoice operations. Injected by Spring's dependency injection facilities.
   */
  @Autowired
  lateinit var invoiceService: InvoiceService

  /**
   * Processes the payment for an invoice corresponding to a specific client.
   *
   * @param clientId The identifier of the client whose invoice is being paid.
   * @param paidAmount The amount that is being paid.
   */
  @PutMapping("/pay")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun payInvoice(@RequestParam clientId: Int, @RequestParam paidAmount: BigDecimal) {
    invoiceService.payInvoice(clientId, paidAmount)
  }

  /**
   * Adjusts the amount of a specific invoice.
   *
   * @param invoiceId The identifier of the invoice to be adjusted.
   * @param amount The amount by which the invoice should be adjusted.
   */
  @PutMapping("/adjust")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun payInvoice(@RequestParam invoiceId: Long, @RequestParam amount: BigDecimal) {
    invoiceService.adjustAmount(invoiceId, amount)
  }

  /**
   * Triggers the immediate generation of invoices.
   */
  @PutMapping("/generateNow")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun generateNow() {
    invoiceService.generateInvoices()
  }

}