package org.dti.se.miniproject1backend1.outers.exceptions.accounts;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
