package com.yabo.addressbook.config;

import com.yabo.addressbook.security.CustomAuthenticationFailureHandler;
import com.yabo.addressbook.security.CustomAuthenticationSuccessHandler;
import com.yabo.addressbook.security.IpLockFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final IpLockFilter ipLockFilter;

    public SecurityConfig(CustomAuthenticationFailureHandler failureHandler,
                          CustomAuthenticationSuccessHandler successHandler,
                          IpLockFilter ipLockFilter) {
        this.failureHandler = failureHandler;
        this.successHandler = successHandler;
        this.ipLockFilter = ipLockFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login.html", "/register.html", "/error.html", "/404.html", "/index.html",
                    "/css/**", "/js/**", "/webjars/**", "/swagger-ui/**", "/v3/api-docs/**",
                    "/swagger-ui.html",                     "/uploads/**", "/captcha", "/api/v1/register/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .failureHandler(failureHandler)
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .permitAll()
                .logoutSuccessUrl("/login.html")
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            // Check IP lock and captcha before authentication
            .addFilterBefore(ipLockFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}