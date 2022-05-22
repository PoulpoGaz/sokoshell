package fr.valax.args.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String name();

    String usage() default "";

    Class<?>[] subCommands() default {};

    Class<?> parent() default Object.class;

    boolean help() default false;
}
