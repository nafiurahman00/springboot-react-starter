package com.example.app.model;

/**
 * User roles. Spring Security authorities are derived as "ROLE_" + name,
 * e.g. ADMIN -> "ROLE_ADMIN". Add more roles here as your app grows.
 */
public enum Role {
    USER,
    ADMIN
}
