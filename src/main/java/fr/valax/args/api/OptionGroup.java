package fr.valax.args.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An option group is only useful for help.
 * It categorizes option.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionGroup {

    /**
     * @return the name of this option
     */
    String name();
}
