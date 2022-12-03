package fr.valax.args;

import fr.valax.args.api.TypeConverter;
import fr.valax.args.utils.TypeException;
import fr.valax.sokoshell.solver.SolverType;

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

    public static final TypeConverter<Byte> BYTE_PRIMITIVE = new NumberTypeConverter<>(Byte::parseByte, (byte) 0);
    public static final TypeConverter<Short> SHORT_PRIMITIVE = new NumberTypeConverter<>(Short::parseShort, (short) 0);
    public static final TypeConverter<Integer> INT_PRIMITIVE = new NumberTypeConverter<>(Integer::parseInt, 0);
    public static final TypeConverter<Long> LONG_PRIMITIVE = new NumberTypeConverter<>(Long::parseLong, 0L);
    public static final TypeConverter<Float> FLOAT_PRIMITIVE = new NumberTypeConverter<>(Float::parseFloat, 0f);
    public static final TypeConverter<Double> DOUBLE_PRIMITIVE = new NumberTypeConverter<>(Double::parseDouble, 0d);

    public static final TypeConverter<Byte> BYTE = new NumberTypeConverter<>(Byte::parseByte, null);
    public static final TypeConverter<Short> SHORT = new NumberTypeConverter<>(Short::parseShort, null);
    public static final TypeConverter<Integer> INT = new NumberTypeConverter<>(Integer::parseInt, null);
    public static final TypeConverter<Long> LONG = new NumberTypeConverter<>(Long::parseLong, null);
    public static final TypeConverter<Float> FLOAT = new NumberTypeConverter<>(Float::parseFloat, null);
    public static final TypeConverter<Double> DOUBLE = new NumberTypeConverter<>(Double::parseDouble, null);

    public static final TypeConverter<Path> PATH = (s) -> {
        if (s == null) {
            return null;
        } else {
            try {
                return Path.of(s);
            } catch (InvalidPathException e) {
                throw new TypeException(s);
            }
        }
    };

    private record NumberTypeConverter<T extends Number>(
            Function<String, T> converter, T whenNull) implements TypeConverter<T> {

        @Override
        public T convert(String value) throws TypeException {
            if (value == null) {
                return whenNull;
            } else {

                try {
                    return converter.apply(value);
                } catch (NumberFormatException e) {
                    throw new TypeException(e);
                }
            }
        }
    }
}
