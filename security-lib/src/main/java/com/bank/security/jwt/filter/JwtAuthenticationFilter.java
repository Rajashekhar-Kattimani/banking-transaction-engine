package com.bank.security.jwt.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Collections;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bank.security.jwt.service.JwtService;
import com.bank.security.constants.JwtClaims;
import com.bank.security.user.SecurityUser;
import com.bank.security.user.UserPrincipal;
import com.bank.security.authorization.Role;
import com.bank.security.authorization.Permission;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        log.debug("JwtAuthenticationFilter - Authorization header: {}", authorizationHeader);

        /*
         * No Authorization header or not a Bearer token.
         * Continue the filter chain.
         */
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(BEARER_PREFIX.length());

        try {

            String username = jwtService.extractUsername(token);

            log.debug("JwtAuthenticationFilter - extracted username: {}", username);

            /*
             * Only authenticate if Spring Security has not
             * already authenticated this request.
             */
            if (username != null
                    && SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                try {
                    // Try to build a rich UserPrincipal from JWT claims
                    Claims claims = jwtService.extractClaim(token, c -> c);

                    // user id (optional)
                    UUID userId = null;
                    Object uidObj = claims.get(JwtClaims.USER_ID);
                    if (uidObj != null) {
                        try {
                            userId = UUID.fromString(uidObj.toString());
                        } catch (Exception e) {
                            // ignore - leave userId null
                        }
                    }

                    // roles (optional)
                    Set<Role> roles = Collections.emptySet();
                    Object rolesObj = claims.get(JwtClaims.ROLES);
                    if (rolesObj instanceof List<?> list && !list.isEmpty()) {
                        roles = list.stream()
                                .map(Object::toString)
                                .map(s -> {
                                    try {
                                        return Role.valueOf(s);
                                    } catch (Exception ex) {
                                        return null;
                                    }
                                })
                                .filter(r -> r != null)
                                .collect(Collectors.toSet());
                    }

                    // permissions (optional)
                    Set<Permission> permissions = Collections.emptySet();
                    Object permsObj = claims.get(JwtClaims.PERMISSIONS);
                    if (permsObj instanceof List<?> plist && !plist.isEmpty()) {
                        permissions = plist.stream()
                                .map(Object::toString)
                                .map(s -> {
                                    try {
                                        return Permission.valueOf(s);
                                    } catch (Exception ex) {
                                        return null;
                                    }
                                })
                                .filter(p -> p != null)
                                .collect(Collectors.toSet());
                    }

                    SecurityUser securityUser = new SecurityUser(
                            userId,
                            username,
                            null,
                            "",
                            roles,
                            permissions,
                            null,
                            null,
                            true,
                            true,
                            true,
                            true
                    );

                    UserPrincipal principal = new UserPrincipal(securityUser);

                    log.debug("JwtAuthenticationFilter - built principal userId={} roles={}", userId, roles);

                    if (jwtService.validateToken(token, principal)) {

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        principal.getAuthorities());

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request));

                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(authentication);
                        log.info("JwtAuthenticationFilter - authenticated user {} from JWT", username);
                        // done
                    } else {
                        log.debug("JwtAuthenticationFilter - token validation failed for constructed principal, falling back to UserDetailsService");
                        // Fallback to loading user details via UserDetailsService
                        UserDetails userDetails =
                                userDetailsService.loadUserByUsername(username);

                        if (jwtService.validateToken(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities());

                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource()
                                            .buildDetails(request));

                            SecurityContextHolder
                                    .getContext()
                                    .setAuthentication(authentication);
                        }
                    }

                } catch (Exception ex) {
                    log.warn("JwtAuthenticationFilter - error building principal from JWT, falling back to UserDetailsService", ex);
                    // If anything goes wrong building a principal, try the old path
                    try {
                        UserDetails userDetails =
                                userDetailsService.loadUserByUsername(username);

                        if (jwtService.validateToken(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities());

                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource()
                                            .buildDetails(request));

                            SecurityContextHolder
                                    .getContext()
                                    .setAuthentication(authentication);
                        }
                    } catch (Exception e) {
                        // swallow - leave unauthenticated
                        log.warn("JwtAuthenticationFilter - fallback UserDetailsService failed", e);
                    }
                }
            }

        } catch (Exception ex) {

            /*
             * Invalid/expired JWT.
             *
             * Do not expose internal JWT parsing details
             * to the client.
             *
             * The request continues without authentication
             * and SecurityFilterChain will return 401 if
             * authentication is required.
             */
            log.debug("JwtAuthenticationFilter - invalid or expired JWT", ex);
        }

        filterChain.doFilter(request, response);
    }
}