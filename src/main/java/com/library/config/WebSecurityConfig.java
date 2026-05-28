package com.library.config;

import com.library.security.AuthFailureHandlerImpl;
import com.library.security.CustomOidcUserService;
import com.library.security.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthFailureHandlerImpl authenticationFailureHandler;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService customUserDetailService;
    private final CustomOidcUserService customOidcUserService;
    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/signin",
                    "/register",
                    "/forgot-password",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/data/**",
                    "/api/auth/**",
                    "/forgot-password",
                    "/verify",
                    "/signin",
                    "/register",
                    "/verify-otp",
                    "/register/resend-otp",
                    "/books",
                    "/book/*",
                    "/category",
                    "/publisher",
                    "/feedback",
                    "/uploads/**",
                    "/intro",
                    "/blog_list",
                    "/blog_list/*",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/signin")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .permitAll()
            )

            .oauth2Login(oauth2 -> oauth2
                .loginPage("/signin")
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/signin?oauth2_error")
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/signin?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}