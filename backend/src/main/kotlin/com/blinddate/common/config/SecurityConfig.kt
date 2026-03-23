package com.blinddate.common.config

import com.blinddate.auth.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/organizer/auth/**").permitAll()
                    .requestMatchers("/api/cafe-owner/auth/**").permitAll()
                    .requestMatchers("/api/bar-owner/auth/**").permitAll()
                    .requestMatchers("/api/admin/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/bars/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/cafes/**").permitAll()
                    .requestMatchers("/api/actions/**").permitAll()
                    .requestMatchers("/api/marketplace/**").hasAnyRole("ORGANIZER", "CAFE_OWNER")
                    .requestMatchers("/api/organizer/**").hasRole("ORGANIZER")
                    .requestMatchers("/api/cafe-owner/**").hasRole("CAFE_OWNER")
                    .requestMatchers("/api/bar-owner/**").hasRole("BAR_OWNER")
                    .requestMatchers("/api/admin/**").hasRole("PLATFORM_ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173", "http://localhost:3000")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", config) }
    }
}
