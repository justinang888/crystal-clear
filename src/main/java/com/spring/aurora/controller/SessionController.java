package com.spring.aurora.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.aurora.config.security.Roles;

/**
 * Who is logged in and what the menu should offer them. Without a login this answers 401
 * (SecurityConfig). The server checks the permission itself; this only hides what would be refused.
 */
@RestController
public class SessionController {

    @GetMapping(value = "/session", produces = "application/json")
    public Map<String, Object> session(Authentication authentication) {
        return Map.of("username", authentication.getName(), "canReconcile", Roles.isOwner(authentication));
    }
}
