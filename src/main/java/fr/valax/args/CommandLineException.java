package fr.valax.args;

public class CommandLineException extends Exception {

    public CommandLineException() {
    }

    public CommandLineException(String message) {
        super(message);
    }

    public CommandLineException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandLineException(Throwable cause) {
        super(cause);
    }

    public CommandLineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
