package com.javainternship.exception;

public class MaxCardsPerUserExceededException extends RuntimeException {

    public MaxCardsPerUserExceededException(String message) {
        super(message);
    }
}

