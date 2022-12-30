package org.messaging;

public class AuthException extends Exception {

    public AuthException()
    {
        super("Auth failure");
    }

    public AuthException(String message)
    {
        super(message);
    }
}