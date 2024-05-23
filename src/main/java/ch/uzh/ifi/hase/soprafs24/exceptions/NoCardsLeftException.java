package ch.uzh.ifi.hase.soprafs24.exceptions;

public class NoCardsLeftException extends RuntimeException {
    public NoCardsLeftException(String message) {
        super(message);
    }
}
