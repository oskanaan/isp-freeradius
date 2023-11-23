package com.thebinaryheap.ispradius.radius

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller that provides an endpoint to trigger a reload of NAS configurations.
 */
@RestController
@RequestMapping("/api/reload-nas")
class ReloadNASController {

  /**
   * Invokes a command to reload NAS configurations using the `radclient` utility.
   *
   * @return A string message indicating the success or failure of the reload operation.
   */
  @PostMapping
  fun reloadNAS(): String {
    return try {
      // Execute the radclient command to reload NAS configurations
      val process = Runtime.getRuntime().exec("radclient -x <radius_server_ip>:<radius_server_port> reload <shared_secret>")
      val exitCode = process.waitFor()

      if (exitCode == 0) {
        "NAS configuration reload initiated successfully."
      } else {
        "Failed to initiate NAS configuration reload."
      }
    } catch (e: Exception) {
      e.printStackTrace()
      "Error occurred while reloading NAS configuration: ${e.message}"
    }
  }
}
