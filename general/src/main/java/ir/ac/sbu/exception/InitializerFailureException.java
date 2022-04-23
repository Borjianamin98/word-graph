package ir.ac.sbu.exception;

public class InitializerFailureException extends RuntimeException {

    public InitializerFailureException(Throwable cause) {
        super("Unable to initialize application.", cause);
    }
}
