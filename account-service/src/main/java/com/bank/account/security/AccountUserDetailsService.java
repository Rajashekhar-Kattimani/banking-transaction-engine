package com.bank.account.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Minimal UserDetailsService for JWT validation in account-service.
 * 
 * Does not load user details from database because:
 * - JWT token signature is already validated by JwtAuthenticationFilter
 * - We only need basic UserDetails for token validation
 * - Account-service doesn't own the user/auth data (auth-service does)
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

    /**
     * Returns a minimal UserDetails object based on username from JWT.
     * 
     * The actual user validation and authorization already happened
     * at the authentication service level. This service just provides
     * the required UserDetails contract for Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username cannot be null or blank");
        }
        
        // Return a minimal UserDetails with no authorities
        // Authorities are embedded in the JWT token itself
        return User.builder()
                .username(username)
                .password("")
                .authorities(java.util.Collections.emptyList())
                .build();
    }
}
