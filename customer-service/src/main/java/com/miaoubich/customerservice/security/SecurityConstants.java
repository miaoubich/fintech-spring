package com.miaoubich.customerservice.security;

import java.util.Map;
import java.util.Set;

public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    // --- Keycloak JWT Claim Keys ---
    public static final String REALM_ACCESS_CLAIM = "realm_access";
    public static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    public static final String ROLES_CLAIM = "roles";
    public static final String FINTECH_CLIENT_ID = "fintech";
    public static final String ROLE_PREFIX = "ROLE_";

    // --- Customer Service Authorities ---
    public static final String CUSTOMER_FULL = "CUSTOMER_FULL";
    public static final String CUSTOMER_READ = "CUSTOMER_READ";
    public static final String CUSTOMER_WRITE = "CUSTOMER_WRITE";
    public static final String CUSTOMER_DELETE = "CUSTOMER_DELETE";
    public static final String CUSTOMER_AUDIT = "CUSTOMER_AUDIT";
    public static final String CUSTOMER_SUSPEND = "CUSTOMER_SUSPEND";
    public static final String CUSTOMER_SUPPORT = "CUSTOMER_SUPPORT";

    // --- Keycloak Role -> Spring Authorities Lookup Table ---
    public static final Map<String, Set<String>> ROLE_TO_AUTHORITIES = Map.ofEntries(
        // SUPER: Full access to all customer endpoints
        Map.entry("super", Set.of(
            CUSTOMER_FULL, CUSTOMER_READ, CUSTOMER_WRITE,
            CUSTOMER_DELETE, CUSTOMER_AUDIT, CUSTOMER_SUSPEND, CUSTOMER_SUPPORT
        )),

        // ADMIN: Read, Write, Delete (no CUSTOMER_FULL)
        Map.entry("admin", Set.of(CUSTOMER_READ, CUSTOMER_WRITE, CUSTOMER_DELETE)),
        Map.entry("admin1", Set.of(CUSTOMER_READ, CUSTOMER_WRITE, CUSTOMER_DELETE)),

        // COMPLIANCE & AUDIT: Read + Audit
        Map.entry("audit", Set.of(CUSTOMER_READ, CUSTOMER_AUDIT)),
        Map.entry("compliance", Set.of(CUSTOMER_READ, CUSTOMER_AUDIT)),
        Map.entry("compliance1", Set.of(CUSTOMER_READ, CUSTOMER_AUDIT)),
        Map.entry("compliance2", Set.of(CUSTOMER_READ, CUSTOMER_AUDIT)),

        // FRAUD: Read + Suspend
        Map.entry("fraud", Set.of(CUSTOMER_READ, CUSTOMER_SUSPEND)),
        Map.entry("fraud1", Set.of(CUSTOMER_READ, CUSTOMER_SUSPEND)),

        // SUPPORT: Read + Support Notes
        Map.entry("support", Set.of(CUSTOMER_READ, CUSTOMER_SUPPORT)),
        Map.entry("support1", Set.of(CUSTOMER_READ, CUSTOMER_SUPPORT)),
        Map.entry("support2", Set.of(CUSTOMER_READ, CUSTOMER_SUPPORT)),

        // STANDARD USERS: Read-only
        Map.entry("user", Set.of(CUSTOMER_READ)),
        Map.entry("user1", Set.of(CUSTOMER_READ)),
        Map.entry("user2", Set.of(CUSTOMER_READ)),
        Map.entry("user3", Set.of(CUSTOMER_READ))
    );
}