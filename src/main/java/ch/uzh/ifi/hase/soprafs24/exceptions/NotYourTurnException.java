package ch.uzh.ifi.hase.soprafs24.exceptions;

public class NotYourTurnException extends RuntimeException {
    public NotYourTurnException(String message) {
        super(message);
    }
}