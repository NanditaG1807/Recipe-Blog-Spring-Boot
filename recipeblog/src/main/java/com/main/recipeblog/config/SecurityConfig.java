
package com.main.recipeblog.config;

import com.main.recipeblog.entity.User;
import com.main.recipeblog.repository.UserRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserRepository repo;

    public SecurityConfig(UserRepository repo) {
        this.repo = repo;
    }


    // Password encryption
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // Get user from MySQL
    @Bean
    public UserDetailsService userDetailsService() {

        return email -> {

            User user = repo.findByEmail(email)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        };
    }


    // Spring Security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // CSRF disable for AJAX like request
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Public pages
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/front-page",
                                "/categories",
                                "/recipe",
                                "/review",
                                "/about-us",
                                "/contact-us",
                                "/forgot-password",
                                "/reset-password",
                                "/search"
                        ).permitAll()

                        // Static files
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/pages/**"
                        ).permitAll()

                        // Wishlist: Login required
                                .requestMatchers(
                                        "/wishlist",
                                        "/wishlist/**"
                                ).authenticated()

                        // Admin only
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // Other requests
                        .anyRequest().authenticated()
                )



                .formLogin(form -> form

                        .loginPage("/login")

                        .successHandler((request, response, authentication) -> {

                            String role = authentication.getAuthorities()
                                    .iterator()
                                    .next()
                                    .getAuthority();

                            if (role.equals("ROLE_ADMIN")) {

                                response.sendRedirect("/front-page");

                            } else if (role.equals("ROLE_USER")) {

                                response.sendRedirect("/front-page");
                            }

                        })

                        .failureUrl("/login?error")

                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );


        return http.build();
    }
}


