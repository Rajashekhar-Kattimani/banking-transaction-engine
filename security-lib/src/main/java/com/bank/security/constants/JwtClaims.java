package com.bank.security.constants;

public final class JwtClaims {

    private JwtClaims() {
    }

    public static final String ROLES = "roles";
    public static final String PERMISSIONS = "permissions";
    public static final String USER_ID = "uid";
    public static final String TOKEN_TYPE = "typ";
    public static final String JTI = "jti";
}