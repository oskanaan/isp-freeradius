package com.thebinaryheap.ispradius.common.controller


import org.springframework.context.annotation.Configuration
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.config.annotation.CorsRegistry

/**
 * Configuration class that implements `RepositoryRestConfigurer` to customize settings and behaviors for
 * Spring Data REST. It registers a validator to be used before the creation and saving of repository entities.
 *
 * @property beanValidator The validator bean that will be used for validating entities before create and save operations.
 */
@Configuration
class RepoRestConfig(private val beanValidator: LocalValidatorFactoryBean) : RepositoryRestConfigurer {
  override fun configureValidatingRepositoryEventListener(v: ValidatingRepositoryEventListener) {
    v.addValidator("beforeCreate", beanValidator)
    v.addValidator("beforeSave", beanValidator)
    super.configureValidatingRepositoryEventListener(v)
  }

  override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration, cors: CorsRegistry?) {
    super.configureRepositoryRestConfiguration(config, cors)
  }
}