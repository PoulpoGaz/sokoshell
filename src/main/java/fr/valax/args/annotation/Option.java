package fr.valax.args.annotation;

import fr.poulpogaz.args.TypeConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

    String[] names();

    String description() default "";

    boolean optional() default true;

    boolean allowDuplicate() default false;

    String defaultValue() default "";

    String argName() default "";

    Class<? extends TypeConverter<?>>[] converter() default {};
}
