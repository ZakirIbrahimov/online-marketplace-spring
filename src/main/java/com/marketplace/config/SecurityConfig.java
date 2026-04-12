package com.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/",
                                                "/products",
                                                "/products/**",
                                                "/register",
                                                "/login",
                                                "/error",
                                                "/css/**",
                                                "/uploads/**",
                                                "/h2-console/**")
                                        .permitAll()
                                        .requestMatchers("/admin/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/merchant/**")
                                        .hasRole("MERCHANT")
                                        .requestMatchers("/cart/**", "/checkout/**", "/orders/**")
                                        .hasRole("SHOPPER")
                                        .anyRequest()
                                        .authenticated())
                .formLogin(form -> form.loginPage("/login").permitAll().defaultSuccessUrl("/", true))
                .logout(
                        logout ->
                                logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                                        .logoutSuccessUrl("/"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
