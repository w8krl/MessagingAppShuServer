package org.messaging;

public class SessionException extends Exception {

    public SessionException()
    {
        super("Session terminated by user");
    }

    public SessionException(String message)
    {
        super(message);
    }
}