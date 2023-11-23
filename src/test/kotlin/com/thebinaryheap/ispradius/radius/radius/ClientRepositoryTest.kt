package com.thebinaryheap.ispradius.radius.radius

import com.thebinaryheap.ispradius.common.ApplicationContextProvider
import com.thebinaryheap.ispradius.radius.repo.ClientEntity
import com.thebinaryheap.ispradius.radius.repo.ClientRepository
import com.thebinaryheap.ispradius.radius.repo.ClientStatus
import com.thebinaryheap.ispradius.radius.repo.ClientSubscriptionModel
import com.thebinaryheap.ispradius.radius.repo.RadcheckRepository
import com.thebinaryheap.ispradius.radius.repo.UniqueUsernameValidator
import com.thebinaryheap.ispradius.settings.repo.City
import com.thebinaryheap.ispradius.settings.repo.CityEntityListener
import com.thebinaryheap.ispradius.settings.repo.CityRepository
import com.thebinaryheap.ispradius.settings.repo.UniqueCityNameValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@DataJpaTest
@Import(CityEntityListener::class, UniqueCityNameValidator::class)
class ClientRepositoryTest {

  @Autowired
  private lateinit var clientRepository: ClientRepository

  @Autowired
  private lateinit var cityRepository: CityRepository

  @Autowired
  private lateinit var entityManager: TestEntityManager

  @Autowired
  private lateinit var applicationContext: ApplicationContext

  @Autowired
  private lateinit var radcheckRepository: RadcheckRepository

  @BeforeEach
  fun setup() {
    ApplicationContextProvider.applicationContext = applicationContext
    cityRepository.save(City(1L, "Test"))
  }

  @Test
  fun `should find clients by full name containing and order by full name`() {
    val city = cityRepository.findAll().first()
    val client1 = ClientEntity()
    client1.fullName = "John Doe"
    client1.username = "john.doe"
    client1.password = "password"
    client1.phoneNumber = "81818181"
    client1.cityId = city.id
    client1.longitude = "1.234"
    client1.latitude = "5.678"
    client1.status = ClientStatus.ACTIVE
    client1.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client1)

    val client2 = ClientEntity()
    client2.fullName = "Jane Smith"
    client2.username = "jane.smith"
    client2.password = "password"
    client2.phoneNumber = "81818181"
    client2.cityId = city.id
    client2.longitude = "9.876"
    client2.latitude = "3.210"
    client2.status = ClientStatus.SUSPENDED
    client2.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client2)

    val client3 = ClientEntity()
    client3.fullName = "Smith Jane"
    client3.username = "smith.jane"
    client3.password = "password"
    client3.phoneNumber = "11111111"
    client3.cityId = city.id
    client3.longitude = "9.876"
    client3.latitude = "3.210"
    client3.status = ClientStatus.SUSPENDED
    client3.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client3)

    entityManager.flush()

    val pageable = PageRequest.of(0, 10, Sort.by("fullName"))

    var result = clientRepository.searchClients("John", null, null, null, pageable)

    assertEquals(1, result.totalElements)
    assertEquals("John Doe", result.content[0].fullName)

    result = clientRepository.searchClients("Jane", null, null, null, pageable)

    assertEquals(2, result.totalElements)
    assertEquals("Jane Smith", result.content[0].fullName)
    assertEquals("Smith Jane", result.content[1].fullName)

    result = clientRepository.searchClients(null, ClientStatus.SUSPENDED, null, null, pageable)

    assertEquals(2, result.totalElements)
    assertEquals("Jane Smith", result.content[0].fullName)
    assertEquals("Smith Jane", result.content[1].fullName)

    result = clientRepository.searchClients(null, null, "jane.smith", null, pageable)

    assertEquals(1, result.totalElements)
    assertEquals("Jane Smith", result.content[0].fullName)

    result = clientRepository.searchClients(null, null, null, "11111111", pageable)

    assertEquals(1, result.totalElements)
    assertEquals("Smith Jane", result.content[0].fullName)
  }

  @Test
  fun `should insert, update, and delete radcheck record along with a client`() {
    val city = cityRepository.findAll().first()
    val client1 = ClientEntity()
    client1.fullName = "John Doe"
    client1.username = "john.doe"
    client1.password = "password"
    client1.phoneNumber = "81818181"
    client1.cityId = city.id
    client1.longitude = "1.234"
    client1.latitude = "5.678"
    client1.status = ClientStatus.ACTIVE
    client1.subscriptionModel = ClientSubscriptionModel.MONTHLY

    assertTrue(radcheckRepository.findByUsernameAndAttribute("john.doe", "MD5-Password").isEmpty())

    entityManager.persist(client1)
    entityManager.flush()

    assertEquals(md5("password"), radcheckRepository.findByUsernameAndAttribute("john.doe", "MD5-Password").first().value)

    client1.password = "password2"
    entityManager.persistAndFlush(client1)

    assertEquals(md5("password2"), radcheckRepository.findByUsernameAndAttribute("john.doe", "MD5-Password").first().value)

    entityManager.remove(client1)
    entityManager.flush()
    assertTrue(radcheckRepository.findByUsernameAndAttribute("john.doe", "SHA-256-Password").isEmpty())
  }

  private fun md5(input: String): String {
    val md = java.security.MessageDigest.getInstance("MD5")
    val hash = md.digest(input.toByteArray(charset("UTF-8")))
    return hash.fold("") { str, it -> str + "%02x".format(it) }
  }


  @Test
  fun `should find clients by status`() {
    val city = cityRepository.findAll().first()

    val client1 = ClientEntity()
    client1.username = "user1"
    client1.fullName = "user1"
    client1.password = "password"
    client1.phoneNumber = "81818181"
    client1.cityId = city.id
    client1.longitude = "1.234"
    client1.latitude = "5.678"
    client1.status = ClientStatus.ACTIVE
    client1.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client1)

    val client2 = ClientEntity()
    client2.username = "user2"
    client2.fullName = "user2"
    client2.password = "password"
    client2.phoneNumber = "81818181"
    client2.cityId = city.id
    client2.longitude = "9.876"
    client2.latitude = "3.210"
    client2.status = ClientStatus.SUSPENDED
    client2.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client2)

    entityManager.flush()

    val result = clientRepository.findByStatus(ClientStatus.ACTIVE)

    assertEquals(1, result.size)
    assertEquals("user1", result[0].username)
  }

  @Test
  fun `should find clients by username ignoring case`() {
    val city = cityRepository.findAll().first()

    val client1 = ClientEntity()
    client1.username = "JohnDoe"
    client1.fullName = "JohnDoe"
    client1.password = "password"
    client1.phoneNumber = "81818181"
    client1.cityId = city.id
    client1.longitude = "1.234"
    client1.latitude = "5.678"
    client1.status = ClientStatus.ACTIVE
    client1.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client1)

    val client2 = ClientEntity()
    client2.username = "JaneSmith"
    client2.fullName = "JaneSmith"
    client2.password = "password"
    client2.phoneNumber = "81818181"
    client2.cityId = city.id
    client2.longitude = "9.876"
    client2.latitude = "3.210"
    client2.status = ClientStatus.ACTIVE
    client2.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client2)

    entityManager.flush()

    val result = clientRepository.findByUsernameIgnoreCase("johndoe")

    assertEquals(1, result.size)
    assertEquals("JohnDoe", result[0].username)
  }

  @Test
  fun `should find all clients`() {
    val city = cityRepository.findAll().first()

    val client1 = ClientEntity()
    client1.username = "user1"
    client1.fullName = "user1"
    client1.password = "password"
    client1.phoneNumber = "81818181"
    client1.cityId = city.id
    client1.longitude = "1.234"
    client1.latitude = "5.678"
    client1.status = ClientStatus.ACTIVE
    client1.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client1)

    val client2 = ClientEntity()
    client2.username = "user2"
    client2.fullName = "user2"
    client2.password = "password"
    client2.phoneNumber = "81818181"
    client2.cityId = city.id
    client2.longitude = "9.876"
    client2.latitude = "3.210"
    client2.status = ClientStatus.ACTIVE
    client2.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client2)

    entityManager.flush()

    val result = clientRepository.findAll()

    assertEquals(2, result.count())
  }

  @Test
  fun `should validate unique username`() {
    val city = cityRepository.findAll().first()

    val client1 = ClientEntity()
    client1.username = "uniqueuser"
    client1.fullName = "uniqueuser"
    client1.password = "password"
    client1.phoneNumber = "81818181"
    client1.cityId = city.id
    client1.longitude = "1.234"
    client1.latitude = "5.678"
    client1.status = ClientStatus.ACTIVE
    client1.subscriptionModel = ClientSubscriptionModel.MONTHLY
    entityManager.persist(client1)

    entityManager.flush()

    val validator = UniqueUsernameValidator()
    validator.clientRepository = clientRepository

    val isValid = validator.isValid("uniqueuser")

    assertFalse(isValid)
  }
}
