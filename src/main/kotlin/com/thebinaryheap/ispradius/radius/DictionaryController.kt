package com.thebinaryheap.ispradius.radius

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for dictionary attribute operations.
 * It provides endpoints for searching dictionary attributes with HAL+JSON response format.
 */
@RestController
@RequestMapping("/dictionary", produces = ["application/hal+json"])
class DictionaryController {

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var pagedResourcesAssembler: PagedResourcesAssembler<DictionaryAttribute>

  /**
   * Searches for dictionary attributes by name, with support for pagination.
   *
   * @param attribute The attribute name to search for.
   * @param page The page number to retrieve.
   * @param size The number of records per page.
   * @return A `ResponseEntity` containing a `PagedModel` of `EntityModel<DictionaryAttribute>`.
   */
  @GetMapping("/attributes/search/findByName", params = ["attribute", "page", "size"])
  fun searchAttributes(@RequestParam attribute: String?, @RequestParam page: Int = 0, @RequestParam size: Int = 20): ResponseEntity<PagedModel<EntityModel<DictionaryAttribute>>> {
    val query = entityManager.createQuery("select DISTINCT NEW com.thebinaryheap.ispradius.radius.DictionaryAttribute(d.attribute) from Dictionary d where LOWER(d.attribute) like LOWER(CONCAT('%', :attribute, '%'))", DictionaryAttribute::class.java)
    query.setFirstResult(page * size)
    query.setMaxResults(size)
    query.setParameter("attribute", attribute)

    val totalResults = entityManager.createQuery("select count(DISTINCT d.attribute) from Dictionary d where LOWER(d.attribute) like LOWER(CONCAT('%', :attribute, '%'))").setParameter("attribute", attribute).singleResult as Long
    return ResponseEntity.ok(pagedResourcesAssembler.toModel(PageImpl<DictionaryAttribute> (query.resultList, org.springframework.data.domain.Pageable.ofSize(size),totalResults)))
  }
}

/**
 * Data class representing a dictionary attribute.
 *
 * @param attribute The name of the attribute.
 */
data class DictionaryAttribute(val attribute: String)