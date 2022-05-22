package fr.valax.args;

import fr.valax.args.utils.TypeException;

/**
 * A type converter is used to convert a string to a T.
 * @param <T> the result
 */
public interface TypeConverter<T> {

    /**
     * @param value the value to convert
     * @return the converted value
     * @throws TypeException it the value can't be converted to T
     */
    T convert(String value) throws TypeException;
}
