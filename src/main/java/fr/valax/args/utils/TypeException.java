package fr.valax.args.utils;

import fr.valax.args.TypeConverter;

/**
 * An exception throws by {@link TypeConverter} when
 * it failed to convert
 * @author PoulpoGaz
 */
public class TypeException extends CommandLineException {

    public TypeException() {
    }

    public TypeException(String message) {
        super(message);
    }

    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeException(Throwable cause) {
        super(cause);
    }

    public TypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
