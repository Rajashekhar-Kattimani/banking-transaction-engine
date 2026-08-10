package com.bank.transaction.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bank.transaction.client.dto.AccountResponse;
import com.bank.transaction.exception.AccountOperationException;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${banking.services.account-service.url}")
            String accountServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(accountServiceUrl)
                .build();
    }

    private String getAuthorizationHeader() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                HttpServletRequest request = servletAttrs.getRequest();
                if (request != null) {
                    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                    return auth;
                }
            }
        } catch (Exception e) {
            // ignore - no request bound
        }
        return null;
    }

    private UUID tryParseUUID(String accountIdentifier) {
        try {
            return UUID.fromString(accountIdentifier);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isValidUUID(String accountIdentifier) {
        return tryParseUUID(accountIdentifier) != null;
    }

    public AccountResponse getAccount(String accountIdentifier) {

        try {

            RestClient.RequestHeadersSpec<?> spec;
            
            // Check if the identifier is a UUID
            if (isValidUUID(accountIdentifier)) {
                // Use UUID-based endpoint
                spec = restClient
                        .get()
                        .uri("/api/v1/accounts/{accountId}",
                                accountIdentifier);
            } else {
                // Use account number endpoint
                spec = restClient
                        .get()
                        .uri("/api/v1/accounts/number/{accountNumber}",
                                accountIdentifier);
            }

            String auth = getAuthorizationHeader();
            if (auth != null) {
                spec = spec.header(HttpHeaders.AUTHORIZATION, auth);
            }

            return spec
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new AccountOperationException(
                                        "Unable to fetch account: "
                                                + accountIdentifier);
                            })
                    .body(AccountResponse.class);

        } catch (AccountOperationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AccountOperationException(
                    "Account Service unavailable for account: "
                            + accountIdentifier);
        }
    }

    public void debit(
            String accountIdentifier,
            BigDecimal amount) {

        try {

            RestClient.RequestHeadersSpec<?> spec;
            
            // Check if the identifier is a UUID
            if (isValidUUID(accountIdentifier)) {
                // Use UUID-based endpoint
                spec = restClient
                        .post()
                        .uri(
                                "/api/v1/accounts/{accountId}/debit",
                                accountIdentifier)
                        .body(amount);
            } else {
                // Use account number endpoint
                spec = restClient
                        .post()
                        .uri(
                                "/api/v1/accounts/number/{accountNumber}/debit",
                                accountIdentifier)
                        .body(amount);
            }

            String auth = getAuthorizationHeader();
            if (auth != null) {
                spec = spec.header(HttpHeaders.AUTHORIZATION, auth);
            }

            spec
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new AccountOperationException(
                                        "Unable to debit account: "
                                                + accountIdentifier);
                            })
                    .toBodilessEntity();

        } catch (AccountOperationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AccountOperationException(
                    "Account debit failed for account: "
                            + accountIdentifier);
        }
    }

    public void credit(
            String accountIdentifier,
            BigDecimal amount) {

        try {

            RestClient.RequestHeadersSpec<?> spec;
            
            // Check if the identifier is a UUID
            if (isValidUUID(accountIdentifier)) {
                // Use UUID-based endpoint
                spec = restClient
                        .post()
                        .uri(
                                "/api/v1/accounts/{accountId}/credit",
                                accountIdentifier)
                        .body(amount);
            } else {
                // Use account number endpoint
                spec = restClient
                        .post()
                        .uri(
                                "/api/v1/accounts/number/{accountNumber}/credit",
                                accountIdentifier)
                        .body(amount);
            }

            String auth = getAuthorizationHeader();
            if (auth != null) {
                spec = spec.header(HttpHeaders.AUTHORIZATION, auth);
            }

            spec
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new AccountOperationException(
                                        "Unable to credit account: "
                                                + accountIdentifier);
                            })
                    .toBodilessEntity();

        } catch (AccountOperationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AccountOperationException(
                    "Account credit failed for account: "
                            + accountIdentifier);
        }
    }
}