package fr.valax.args.api;

import fr.valax.args.utils.TypeException;

/**
 * A type converter is used to convert a string to a T.
 * @param <T> the result
 * @author PoulpoGaz
 */
public interface TypeConverter<T> {

    /**
     * @param value the value to convert
     * @return the converted value
     * @throws TypeException it the value can't be converted to T
     */
    T convert(String value) throws TypeException;
}
