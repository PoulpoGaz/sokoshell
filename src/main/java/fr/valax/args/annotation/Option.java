package fr.valax.args.annotation;

import fr.valax.args.TypeConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare a field as an option for a {@link Command}
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
     * @return the default value, it will be then parsed
     *         by a TypeConverter if specified
     * @see #converter()
     * @see TypeConverter
     */
    String defaultValue() default "";

    /**
     * @return what is the purpose of this command
     * @see fr.valax.args.HelpFormatter
     */
    String description() default "";

    /**
     * @return the name of the argument
     * @see fr.valax.args.HelpFormatter
     */
    String argName() default "";

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
