package com.spring.aurora.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String[] PUBLIC = new String[] { "/login", "/logout", "/error", "/favicon.ico",
    "/h2-console/**" };
    private static final String[] ASSETS = new String[] { "/js/*", "/js/**", "/css/*", "/css/**" };

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDetailsService auroraUserDetailsService;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(this.auroraUserDetailsService).passwordEncoder(this.passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers(PUBLIC).permitAll().anyRequest().authenticated().and().formLogin()
        // .loginPage("/login").loginProcessingUrl("/login")
        .defaultSuccessUrl("/customers/list", true)
        .failureUrl("/login?error").and().logout();
        // for /h2-console endpoint
        http.headers().frameOptions().sameOrigin();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(ASSETS);
    }
}