package com.bank.transaction.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice(basePackages = "com.bank.transaction")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
            handleTransactionNotFound(
                    TransactionNotFoundException exception) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage());
    }

    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<Map<String, Object>>
            handleInvalidTransfer(
                    InvalidTransferException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage());
    }

    @ExceptionHandler(AccountOperationException.class)
    public ResponseEntity<Map<String, Object>>
            handleAccountOperation(
                    AccountOperationException exception) {

        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
            handleValidation(
                    MethodArgumentNotValidException exception) {

        Map<String, String> errors =
                new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()));

        Map<String, Object> response =
                new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation failed");
        response.put("errors", errors);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
            handleGeneric(Exception exception) {

        // Log the unexpected exception for diagnostics
        logger.error("Unhandled exception caught by GlobalExceptionHandler", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>>
            buildResponse(
                    HttpStatus status,
                    String message) {

        Map<String, Object> response =
                new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);

        return ResponseEntity
                .status(status)
                .body(response);
    }
}