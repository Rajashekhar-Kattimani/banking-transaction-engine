package com.bank.security.constants;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String AUTHORIZATION = "Authorization";

    public static final String BEARER = "Bearer ";

    public static final String ISSUER = "BANKING-TRANSACTION-ENGINE";

    public static final String TOKEN_TYPE = "JWT";
    
    public static final String ROLE_PREFIX = "ROLE_";
    
    // JWT Claims
    public static final String USERNAME = "username";
    public static final String ROLES = "roles";
    public static final String PERMISSIONS = "permissions";

    // Token Types
    public static final String ACCESS_TOKEN = "ACCESS";
    public static final String REFRESH_TOKEN = "REFRESH";

    // Cache Keys
    public static final String TOKEN_BLACKLIST = "token:blacklist:";
    public static final String REFRESH_TOKEN_CACHE = "refresh:";
    public static final String LOGIN_ATTEMPT = "login-attempt:";
    public static final String OTP = "otp:";

}