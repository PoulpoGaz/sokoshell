package fr.valax.args.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare a field as an option for a {@link Command}
 * @author PoulpoGaz
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

    /**
     * Important: a hyphen is automatically added.
     * @return the names of the option.
     */
    String[] names();

    /**
     * @return is this option required or not?
     */
    boolean optional() default true;

    /**
     * A field annotated with an option with allowDuplicate
     * set to true <strong>should</strong> should be an array
     * @return true if the user can use more than one this option
     */
    boolean allowDuplicate() default false;

    /**
     * If the option doesn't need an argument, the field should be a boolean
     * @return true if the option need an argument
     */
    boolean hasArgument() default false;

    /**
     * Only the first value is used for allowing null value
     * @return the default value, it will be then parsed
     *         by a TypeConverter if specified
     * @see #converter()
     * @see TypeConverter
     */
    String[] defaultValue() default {};

    /**
     * Only the first value is used for allowing null value
     * @return what is the purpose of this command?
     */
    String[] description() default {};

    /**
     * Only the first value is used for allowing null value
     * @return the name of the argument
     */
    String[] argName() default {};

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
