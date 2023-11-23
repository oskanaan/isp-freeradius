package com.thebinaryheap.ispradius.security

import com.thebinaryheap.ispradius.security.repo.UserRepository
import com.thebinaryheap.ispradius.security.repo.UserStatus
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.filter.OncePerRequestFilter


/**
 * Security configuration for the ISP application. All users must be authenticated and has one of the following roles:
 * - EMPLOYEE: has access to invoices.
 * - ADMIN: has full access.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

  @Autowired
  private lateinit var authenticationEntryPoint: AuthenticationEntryPoint

  @Autowired
  private lateinit var jwtTokenProvider: JwtTokenProvider

  @Bean
  fun configure(http: HttpSecurity) : SecurityFilterChain {
    http.cors(Customizer.withDefaults())
      .authorizeHttpRequests { request ->
        request
          .requestMatchers(AntPathRequestMatcher("/auth/login")).permitAll()
          .requestMatchers(AntPathRequestMatcher("/error")).permitAll()

          .requestMatchers(AntPathRequestMatcher("/configuration/settings", HttpMethod.GET.name())).hasAnyRole("ADMIN", "EMPLOYEE")
          .requestMatchers(AntPathRequestMatcher("/configuration/settings")).hasRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/clients")).hasAnyRole("ADMIN", "EMPLOYEE")
          .requestMatchers(AntPathRequestMatcher("/clients/**")).hasAnyRole("ADMIN", "EMPLOYEE")
          .requestMatchers(AntPathRequestMatcher("/clients/search/clients")).hasAnyRole("ADMIN", "EMPLOYEE")

          .requestMatchers(AntPathRequestMatcher("/groups")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/groups/**")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/groups/search/findByGroupNameIgnoreCase")).hasAnyRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/nas")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/nas/**")).hasAnyRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/invoices", HttpMethod.GET.name())).hasAnyRole("ADMIN", "EMPLOYEE")
          .requestMatchers(AntPathRequestMatcher("/invoices/**", HttpMethod.GET.name())).hasAnyRole("ADMIN", "EMPLOYEE")
          .requestMatchers(AntPathRequestMatcher("/invoices/pay", HttpMethod.PUT.name())).hasAnyRole("ADMIN", "EMPLOYEE")
          .requestMatchers(AntPathRequestMatcher("/invoices")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/invoices/**")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/invoices/search/findByClientId")).hasAnyRole("ADMIN", "EMPLOYEE")

          .requestMatchers(AntPathRequestMatcher("/radreply")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/radreply/**")).hasAnyRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/radusergroups")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/radusergroups/**")).hasAnyRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/radgroupreply")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/radgroupreply/**")).hasAnyRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/users")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/users/**")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/users/search/searchUsers")).hasAnyRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/radgroupcheck")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/radgroupcheck/**")).hasAnyRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/cities")).hasRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/cities/search/**")).hasRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/cities/**")).hasRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/radcheck")).hasRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/radcheck/search/**")).hasRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/radcheck/**")).hasRole("ADMIN")

          .requestMatchers(AntPathRequestMatcher("/dictionary/attributes/search/findByName")).hasAnyRole("ADMIN")
          .requestMatchers(AntPathRequestMatcher("/dictionary/attributes/search/findByName/**")).hasAnyRole("ADMIN")
      }.exceptionHandling{ it.authenticationEntryPoint(authenticationEntryPoint)}
      .addFilterBefore(JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter::class.java)
      .formLogin { it.disable() }.csrf { it.disable() }

    return http.build()
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }

}

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

  override fun loadUserByUsername(username: String): UserDetails {
    val users = userRepository.findByUsername(username)
    if (users.isEmpty()) {
      throw UsernameNotFoundException("User $username not found.")
    }

    val user = users.first()

    val userDetails = object: UserDetails {
      override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return user.roles!!.split(",").map {role -> GrantedAuthority { "ROLE_$role" }}.toMutableList()
      }

      override fun getPassword(): String {
        return user.password!!
      }

      override fun getUsername(): String {
        return user.username!!
      }

      override fun isAccountNonExpired(): Boolean {
        return true
      }

      override fun isAccountNonLocked(): Boolean {
        return true
      }

      override fun isCredentialsNonExpired(): Boolean {
        return true
      }

      override fun isEnabled(): Boolean {
        return user.status == UserStatus.ACTIVE
      }

      override fun toString(): String {
        return "[username=$username, roles=${user.roles}, enabled=${isEnabled}]"
      }

    }

    return userDetails
  }

}



@Component
class JwtTokenProvider {
  @Value("\${app.jwtSecret}")
  private val jwtSecret: String? = null

  @Value("\${app.jwtExpirationInMs}")
  private val jwtExpirationInMs: Long = 0

  @Autowired
  private lateinit var customUserDetailsService: CustomUserDetailsService
  fun generateToken(username: String?): String {
    val now = Date()
    val expiryDate = Date(now.time + jwtExpirationInMs)
    return Jwts.builder()
      .setSubject(username)
      .setIssuedAt(now)
      .setExpiration(expiryDate)
      .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, jwtSecret)
      .compact()
  }

  fun getUsernameFromToken(token: String?): String {
    return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject()
  }

  fun resolveToken(request: HttpServletRequest): String? {
    val bearerToken = request.getHeader("Authorization")
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7) // Remove "Bearer " prefix
    }
    return null
  }

  fun validateToken(token: String): Boolean {
    return try {
      val claims = extractClaims(token)
      !claims.expiration.before(Date()) // Check if token is expired
    } catch (e: Exception) {
      false
    }
  }

  private fun extractClaims(token: String): Claims {
    return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).body
  }

  fun getAuthentication(token: String): Authentication? {
    val userDetails = getUserDetailsFromToken(token)
    return userDetails?.let { createAuthentication(userDetails) }
  }

  private fun createAuthentication(userDetails: UserDetails): Authentication {
    return UsernamePasswordAuthenticationToken(
      userDetails,
      null,
      userDetails.authorities
    )
  }

  private fun getUserDetailsFromToken(token: String): UserDetails? {
    val username = extractClaim(token, Claims::getSubject)
    return username.let { customUserDetailsService.loadUserByUsername(it!!) }
  }

  private fun <R> extractClaim(token: String, claimsResolver: java.util.function.Function<Claims, R>): R? {
    val claims = extractClaims(token)
    return claimsResolver.apply(claims)
  }
}


class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider) : OncePerRequestFilter() {

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    val token = jwtTokenProvider.resolveToken(request)

    if (token != null && jwtTokenProvider.validateToken(token)) {
      val auth = jwtTokenProvider.getAuthentication(token)
      if (auth != null) {
        SecurityContextHolder.getContext().authentication = auth
      }
    }

    filterChain.doFilter(request, response)
  }
}


@RestController
class AuthController {

  @Autowired
  private lateinit var tokenProvider: JwtTokenProvider

  @Autowired
  private lateinit var customUserDetailsService: CustomUserDetailsService

  @Autowired
  private lateinit var passwordEncoder: PasswordEncoder

  @Autowired
  private val userDetailsService: UserDetailsService? = null
  @PostMapping("/auth/login")
  fun authenticateUser(@RequestBody loginForm: LoginForm): ResponseEntity<*> {
    val authentication = authenticate(
      UsernamePasswordAuthenticationToken(
        loginForm.username,
        loginForm.password
      )
    )
    SecurityContextHolder.getContext().authentication = authentication
    return if (authentication != null) {
      val jwt = tokenProvider.generateToken(authentication.name)
      ResponseEntity.ok(
        JwtAuthenticationResponse(
          jwt,
          if (authentication.authorities.any { it.authority == "ROLE_ADMIN" }) "ADMIN" else "EMPLOYEE",
          loginForm.username)
      )
    } else {
      ResponseEntity.notFound().build<Any>()
    }
  }

  fun authenticate(authentication: Authentication): Authentication? {
    val username: String = authentication.principal.toString()
    val password: String = authentication.credentials.toString()
    val user = customUserDetailsService.loadUserByUsername(username)
    if (!passwordEncoder.matches(password, user.password)) {
      throw BadCredentialsException("1000")
    }
    if (!user.isEnabled) {
      throw DisabledException("1001")
    }

    return UsernamePasswordAuthenticationToken(
      username,
      null,
      user.authorities
    )
  }

}


@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
  @Throws(IOException::class, ServletException::class)
  override fun commence(
    request: HttpServletRequest, response: HttpServletResponse,
    authException: AuthenticationException
  ) {
    // Handle unauthorized access here and send a proper response
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
  }
}

data class LoginForm(
  var username: String,
  var password: String
)


data class JwtAuthenticationResponse(var token: String, var type: String, var username: String)

