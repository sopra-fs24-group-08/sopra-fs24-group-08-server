package ch.uzh.ifi.hase.soprafs24.exceptions;

public class IncompleteGameDataException extends RuntimeException {
    public IncompleteGameDataException(String message) {
        super(message);
    }
}
