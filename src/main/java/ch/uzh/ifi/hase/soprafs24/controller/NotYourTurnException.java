// NotYourTurnException.java
package ch.uzh.ifi.hase.soprafs24.controller;

public class NotYourTurnException extends RuntimeException {
    public NotYourTurnException(String message) {
        super(message);
    }
}

