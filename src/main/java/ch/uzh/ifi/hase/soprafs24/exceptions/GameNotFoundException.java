package ch.uzh.ifi.hase.soprafs24.exceptions;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String message) {
        super(message);
    }

}