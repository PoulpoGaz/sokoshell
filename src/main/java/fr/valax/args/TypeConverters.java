package fr.valax.args;

import fr.valax.args.utils.TypeException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Basic type converters.
 * They are not added by default. You must use
 * {@link CommandLineBuilder#addDefaultConverters()} to add them
 * @author PoulpoGaz
 */
public class TypeConverters {

    public static final TypeConverter<String> STRING = s -> s;

    public static final TypeConverter<Byte> BYTE = new NumberTypeConverter<>(Byte::parseByte);
    public static final TypeConverter<Short> SHORT = new NumberTypeConverter<>(Short::parseShort);
    public static final TypeConverter<Integer> INT = new NumberTypeConverter<>(Integer::parseInt);
    public static final TypeConverter<Long> LONG = new NumberTypeConverter<>(Long::parseLong);
    public static final TypeConverter<Float> FLOAT = new NumberTypeConverter<>(Float::parseFloat);
    public static final TypeConverter<Double> DOUBLE = new NumberTypeConverter<>(Double::parseDouble);

    public static final TypeConverter<Path> PATH = (s) -> {
        try {
            return Path.of(s);
        } catch (InvalidPathException e) {
            throw new TypeException(s);
        }
    };

    private record NumberTypeConverter<T extends Number>(
            Function<String, T> converter) implements TypeConverter<T> {

        @Override
        public T convert(String value) throws TypeException {
            try {
                return converter.apply(value);
            } catch (NumberFormatException e) {
                throw new TypeException(e);
            }
        }
    }
}
