package com.spring.aurora.config.security;

import java.security.NoSuchAlgorithmException;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class BCryptEncoder {
	
	@Bean
    public PasswordEncoder passwordEncoder() throws NoSuchAlgorithmException {
        int strength = 11;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(strength);
        return encoder;
    }
	
}