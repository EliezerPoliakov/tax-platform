package com.poliakov.taxplatform.identity;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Email address is already registered");
    }
}
