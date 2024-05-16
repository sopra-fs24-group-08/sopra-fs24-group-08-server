package ch.uzh.ifi.hase.soprafs24.exceptions;

public class SquareOccupiedException extends RuntimeException {
    public SquareOccupiedException(String message) {
        super(message);
    }

    public SquareOccupiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
