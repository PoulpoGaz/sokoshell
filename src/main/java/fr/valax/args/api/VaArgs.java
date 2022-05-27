package fr.valax.args.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declaring a field as VaArgs means that it will receive all
 * arguments that are not attached to an option.
 * @author PoulpoGaz
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VaArgs {

    /**
     * It returns a 1-length array containing a TypeConverter.
     * The TypeConverter is used to parse the default value or
     * the value given by the user.
     * It has priority over the global TypeConverter of {@link fr.valax.args.CommandLine}
     * @return <strong>The</strong> converter
     * @see TypeConverter
     * @see fr.valax.args.CommandLine
     */
    Class<? extends TypeConverter<?>>[] converter() default {};
}
