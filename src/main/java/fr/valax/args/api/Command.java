package fr.valax.args.api;

/**
 * @param <T> the type of the output
 * @author PoulpoGaz
 */
public interface Command<T> {

    /**
     * @return the output of this command
     */
    T execute();

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
}
