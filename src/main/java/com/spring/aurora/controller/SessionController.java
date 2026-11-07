package com.spring.aurora.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Who is logged in, for the React app's menu. Without a login this answers 401 (SecurityConfig). */
@RestController
public class SessionController {

    @GetMapping(value = "/session", produces = "application/json")
    public Map<String, String> session(Principal principal) {
        return Map.of("username", principal.getName());
    }
}
