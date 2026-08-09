package com.bank.transaction.client;

import java.math.BigDecimal;

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

    public AccountResponse getAccount(String accountNumber) {

        try {

            RestClient.RequestHeadersSpec<?> spec = restClient
                    .get()
                    .uri("/api/v1/accounts/number/{accountNumber}",
                            accountNumber);

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
                                                + accountNumber);
                            })
                    .body(AccountResponse.class);

        } catch (AccountOperationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AccountOperationException(
                    "Account Service unavailable for account: "
                            + accountNumber);
        }
    }

    public void debit(
            String accountNumber,
            BigDecimal amount) {

        try {

            RestClient.RequestHeadersSpec<?> spec = restClient
                    .post()
                    .uri(
                            "/api/v1/accounts/number/{accountNumber}/debit",
                            accountNumber)
                    .body(amount);

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
                                                + accountNumber);
                            })
                    .toBodilessEntity();

        } catch (AccountOperationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AccountOperationException(
                    "Account debit failed for account: "
                            + accountNumber);
        }
    }

    public void credit(
            String accountNumber,
            BigDecimal amount) {

        try {

            RestClient.RequestHeadersSpec<?> spec = restClient
                    .post()
                    .uri(
                            "/api/v1/accounts/number/{accountNumber}/credit",
                            accountNumber)
                    .body(amount);

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
                                                + accountNumber);
                            })
                    .toBodilessEntity();

        } catch (AccountOperationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AccountOperationException(
                    "Account credit failed for account: "
                            + accountNumber);
        }
    }
}