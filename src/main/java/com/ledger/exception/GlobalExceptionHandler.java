package com.ledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleAccountNotFound(AccountNotFoundException ex) {
        return ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
    }

    @ExceptionHandler(AccountNotActiveException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleAccountNotActive(AccountNotActiveException ex) {
        return ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Account Not Active", ex.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleInsufficientFunds(InsufficientFundsException ex) {
        return ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Insufficient Funds", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Validation Failed", message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage());
    }
}
