package com.thebinaryheap.ispradius.invoicing

import com.thebinaryheap.ispradius.invoicing.repo.InvoiceRepository
import java.math.BigDecimal
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.springframework.context.MessageSource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class InvoiceControllerTest {

  @Mock
  private lateinit var invoiceService: InvoiceService

  @Mock
  private lateinit var invoiceRepository: InvoiceRepository

  @Mock
  private lateinit var messageSource: MessageSource

  @InjectMocks
  private lateinit var invoiceController: InvoiceController

  private lateinit var mockMvc: MockMvc

  @BeforeEach
  fun setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(invoiceController).build()
  }

  @Test
  fun `test payInvoice successful`() {
    mockMvc.perform(
      put("/invoices/pay")
        .param("clientId", "1")
        .param("paidAmount", "100")
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNoContent)

    verify(invoiceService).payInvoice(1, BigDecimal(100))
  }
}
