package ch.uzh.ifi.hase.soprafs24.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>("Illegal argument: " + ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        return new ResponseEntity<>("Illegal state: " + ex.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            TransactionSystemException.class,
            GameNotFoundException.class,
            PlayerNotFoundException.class,
            CardNotFoundException.class,
            GameNotFinishedException.class,
            IncompleteGameDataException.class,
            NotYourTurnException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<Object> handleCustomExceptions(RuntimeException ex, WebRequest request) {
        log.error("Exception: {}", ex.getMessage());
        HttpStatus status = determineHttpStatus(ex);
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), status, request);
    }

    private HttpStatus determineHttpStatus(RuntimeException ex) {
        if (ex instanceof TransactionSystemException) return HttpStatus.CONFLICT;
        if (ex instanceof GameNotFoundException || ex instanceof PlayerNotFoundException || ex instanceof CardNotFoundException || ex instanceof UserNotFoundException)
            return HttpStatus.NOT_FOUND;
        if (ex instanceof GameNotFinishedException || ex instanceof IncompleteGameDataException || ex instanceof NotYourTurnException)
            return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
