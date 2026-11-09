package com.spring.aurora.config.security;

import org.springframework.security.core.Authentication;

/**
 * The roles in users.role. ROLE_OWNER can do everything, including the bank reconciliation; any
 * other value (ROLE_ADMIN, ROLE_USER) is a bookkeeper: everything except the reconciliation.
 */
public final class Roles {

    /** For hasRole(...), which adds the ROLE_ prefix. */
    public static final String OWNER = "OWNER";
    public static final String OWNER_AUTHORITY = "ROLE_" + OWNER;
    /** What a bookkeeper login is stored as. */
    public static final String BOOKKEEPER_AUTHORITY = "ROLE_ADMIN";

    private Roles() {
    }

    public static boolean isOwner(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> OWNER_AUTHORITY.equals(a.getAuthority()));
    }
}
