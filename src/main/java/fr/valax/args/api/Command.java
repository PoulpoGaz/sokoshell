package fr.valax.args.api;

public interface Command<T> {

    /**
     * @return the name of the command
     */
    String getName();

    /**
     * @return A text to explains the user how to use the command.
     */
    String getUsage();

    /**
     * @return true to automatically add -h/--help option
     */
    boolean addHelp();

    T execute();
}
