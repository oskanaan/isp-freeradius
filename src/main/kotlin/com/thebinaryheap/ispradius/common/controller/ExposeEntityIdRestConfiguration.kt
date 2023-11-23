package com.thebinaryheap.ispradius.common.controller

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry

/**
 * Configuration class that customizes the repository REST configuration for exposing entity IDs.
 * By default, Spring Data REST does not expose entity IDs in the responses; this class configures
 * the application to include them.
 */
@Configuration
class ExposeEntityIdRestConfiguration : RepositoryRestConfigurer {
  @Autowired
  private lateinit var entityManager: EntityManager

  override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration, cors: CorsRegistry) {
    val entityTypes = entityManager.metamodel.entities.map { it.javaType }
    config.exposeIdsFor(*entityTypes.toTypedArray())
  }
}