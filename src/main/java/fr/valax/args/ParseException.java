package fr.valax.args;

/**
 * Base class for parsing exception.
 * They may occur when parsing arguments or
 * when converting a value
 *
 * @see TypeException
 */
public class ParseException extends CommandLineException {

    public ParseException() {
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}