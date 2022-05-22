package fr.valax.args;

public interface TypeConverter<T> {

    T convert(String value) throws TypeException;
}
