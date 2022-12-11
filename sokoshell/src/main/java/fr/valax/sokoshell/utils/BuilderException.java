package fr.valax.sokoshell.utils;

/**
 * Signals that a builder can't build an object
 * @author PoulpoGaz
 */
public class BuilderException extends IllegalStateException {

    public BuilderException() {
    }

    public BuilderException(String message) {
        super(message);
    }

    public BuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderException(Throwable cause) {
        super(cause);
    }
}
