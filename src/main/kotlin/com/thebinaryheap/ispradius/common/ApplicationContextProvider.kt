package com.thebinaryheap.ispradius.common

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * A component that holds the reference to the `ApplicationContext`. It provides a static way to access
 * beans from non-spring-managed classes or instances. The class implements `ApplicationContextAware`
 * which allows it to receive the `ApplicationContext` upon initialization.
 */
@Component
class ApplicationContextProvider : ApplicationContextAware {

  /**
   * Sets the ApplicationContext that this object runs in.
   *
   * This method is called after the population of normal bean properties but before an init callback
   * like `InitializingBean's` `afterPropertiesSet` or a custom init-method. It can also be customized
   * in a bean factory post-processor.
   *
   * @param applicationContext the ApplicationContext object to be used by this class.
   * @throws BeansException in case of context initialization errors.
   */
  @Throws(BeansException::class)
  override fun setApplicationContext(applicationContext: ApplicationContext) {
    Companion.applicationContext = applicationContext
  }

  companion object {
    var applicationContext: ApplicationContext? = null
  }
}