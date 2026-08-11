package com.bank.security.jwt;

public final class JwtClaims {

    private JwtClaims() {
    }

    public static final String USER_ID = "userId";

    public static final String USERNAME = "username";

    public static final String EMAIL = "email";

    public static final String ROLES = "roles";

    public static final String PERMISSIONS = "permissions";

    public static final String BRANCH_CODE = "branchCode";

    public static final String TENANT_ID = "tenantId";
    
    public static final String TOKEN_TYPE = "typ";

    public static final String JTI = "jti";

}