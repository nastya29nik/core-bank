package com.niknastacy.corebank.exception;

public class BankOperationException extends RuntimeException {
    public BankOperationException(String message) {
        super(message);
    }
}
