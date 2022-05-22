package fr.valax.args.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used for declaring a command.
 * A class annotated with Command should be a {@link Runnable}
 * or {@link java.util.function.Supplier}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandOld {

    /**
     * @return the name of the command
     */
    String name();

    /**
     * @return An array containing all sub commands.
     *         Sub-commands should set their parent field to this command
     */
    Class<?>[] subCommands() default {};

    /**
     * @return The parent of this command
     */
    Class<?> parent() default Object.class;

    /**
     * If true, it automatically adds a --help/-h option with the specified {@link #usage()}
     * Moreover, when the parser failed to parse options, it shows help
     * @return true to add a -help/-h option
     */
    boolean help() default false;

    /**
     * @return A text to explains the user how to use the command.
     *         Only useful when {@link #help()} returns true
     */
    String usage() default "";
}
