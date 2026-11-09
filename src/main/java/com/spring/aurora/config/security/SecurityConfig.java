package com.spring.aurora.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.DispatcherType;

/**
 * Every page and every piece of data requires a logged-in user. Users come from the users table
 * through AuroraUserDetailsService, with passwords checked by the BCryptEncoder bean.
 *
 * The React app logs in by posting the username and password to /login and gets 200 or 401 back;
 * logging out is a POST to /logout. CSRF protection uses the XSRF-TOKEN cookie, which the app
 * sends back in the X-XSRF-TOKEN header (Spring Security's SPA mode).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        MediaTypeRequestMatcher dataRequest = new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON);
        dataRequest.setIgnoredMediaTypes(java.util.Set.of(MediaType.ALL));

        http.authorizeHttpRequests(auth -> auth
                // Sending index.html, or the error page, happens inside a request that was already checked.
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                // The React app itself and its files load before anyone logs in; its data does not.
                .requestMatchers("/index.html", "/assets/**", "/favicon.svg", "/login", "/error").permitAll()
                // Only the owner sees and fills in the bank reconciliation; every other login is a bookkeeper.
                .requestMatchers("/expenses/reconciliation", "/expenses/reconciliation/**").hasRole(Roles.OWNER)
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.spa())
            // Issue the XSRF-TOKEN cookie on every response, so it is there before the first post (login).
            .addFilterAfter(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                        jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain chain)
                        throws jakarta.servlet.ServletException, java.io.IOException {
                    CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                    if (token != null) {
                        token.getToken();
                    }
                    chain.doFilter(request, response);
                }
            }, CsrfFilter.class)
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                .failureHandler((request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                .permitAll())
            .logout(logout -> logout.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))
            .exceptionHandling(errors -> errors
                // Data requests get 401 so the app can show its login screen; page loads go to /login.
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), dataRequest));
        return http.build();
    }
}
