package fr.valax.args.api;

public interface Command<T> {

    /**
     * @return the name of the command
     */
    String getName();

    /**
     * @return A text to explains the user how to use the command.
     *         Only useful when {@link #help()} returns true
     */
    String getUsage();

    boolean help();

    T execute();
}
