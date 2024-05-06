package ch.uzh.ifi.hase.soprafs24.exceptions;

public class GameNotFinishedException extends RuntimeException {
    public GameNotFinishedException(String message) {
        super(message);
    }
}