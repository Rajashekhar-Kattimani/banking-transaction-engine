package com.bank.transaction.client;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.bank.transaction.client.dto.AccountResponse;
import com.bank.transaction.exception.AccountOperationException;

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

    public AccountResponse getAccount(String accountNumber) {

        try {

            return restClient
                    .get()
                    .uri("/api/accounts/number/{accountNumber}",
                            accountNumber)
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

            restClient
                    .post()
                    .uri(
                            "/api/accounts/number/{accountNumber}/debit",
                            accountNumber)
                    .body(amount)
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

            restClient
                    .post()
                    .uri(
                            "/api/accounts/number/{accountNumber}/credit",
                            accountNumber)
                    .body(amount)
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