package com.bank.account.security;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bank.security.user.UserPrincipal;

@Service
public class AccountSecurityService {

    public UUID getAuthenticatedUserId(
            UserPrincipal principal) {

        if (principal == null) {
            throw new AccessDeniedException(
                    "Authenticated user is required");
        }

        UUID userId = principal.getUserId();

        if (userId == null) {
            throw new AccessDeniedException(
                    "Authenticated user ID is missing");
        }

        return userId;
    }

    public void verifyOwnership(
            UUID accountCustomerId,
            UserPrincipal principal) {

        UUID authenticatedUserId =
                getAuthenticatedUserId(principal);

        if (!authenticatedUserId.equals(accountCustomerId)) {
            throw new AccessDeniedException(
                    "You are not authorized to access this account");
        }
    }
}