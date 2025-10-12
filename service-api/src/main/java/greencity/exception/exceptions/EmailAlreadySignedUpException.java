package greencity.exception.exceptions;

public class EmailAlreadySignedUpException extends RuntimeException {
    public EmailAlreadySignedUpException(String message) {
        super(message);
    }
}
